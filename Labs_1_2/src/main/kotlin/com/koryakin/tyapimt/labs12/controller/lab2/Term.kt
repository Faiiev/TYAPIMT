package com.koryakin.tyapimt.labs12.controller.lab2

import com.koryakin.tyapimt.labs12.model.Delimiter
import com.koryakin.tyapimt.labs12.model.Lex
import com.koryakin.tyapimt.labs12.model.LexType
import tornadofx.*

class Term(controller: Lab2Controller) : BaseClass(controller) {

    var lastLex: Lex? = null
        private set

    var afterClose = false
        private set

    override fun start(lex: Lex?) {
        if (reset) {
            isReady = true
            return
        }
        val factor = Factor(controller)
        factor.isReadyProperty.onChangeOnce {
            if (controller.errorStr.isNullOrEmpty()) {
                number = factor.number
                waitOperation()
            } else isReady = true
        }
        factor.start(lex)
    }

    private fun waitOperation() {
        controller.lastLexProperty.onChangeOnce {
            lastLex = it
            if (reset) {
                isReady = true
                return@onChangeOnce
            }
            when {
                it == null -> isReady = true
                it.type != LexType.DELIMITER -> {
                    controller.errorStr = "Ошибка: ожидался знак операции"
                    isReady = true
                }
                else -> {
                    val delimiter = Delimiter.getByIndex(it.index)
                    onDelimiter(delimiter)
                }
            }
        }
    }

    private fun onDelimiter(delimiter: Delimiter) {
        if (reset) {
            isReady = true
            return
        }
        when (delimiter) {
            Delimiter.PLUS, Delimiter.MINUS -> isReady = true
            Delimiter.MULTIPLY, Delimiter.DIVIDE -> waitNextFactor(delimiter)
            Delimiter.PARENTHESES_OPEN -> {
                controller.errorStr = "Ошибка: ожидался знак операции"
                isReady = true
            }
            Delimiter.PARENTHESES_CLOSE -> {
                controller.openCloseParentheses--
                afterClose = true
                if (controller.openCloseParentheses < 0) controller.errorStr = "Ошибка: лишняя закрывающая скобка"
                isReady = true
            }
        }
    }

    private fun waitNextFactor(prevDelimiter: Delimiter) {
        controller.lastLexProperty.onChangeOnce {
            if (reset) {
                isReady = true
                return@onChangeOnce
            }
            if (it == null) {
                controller.errorStr = "Ошибка: неожиданное завершение строки"
                isReady = true
                return@onChangeOnce
            }
            val factor = Factor(controller)
            factor.isReadyProperty.onChangeOnce { onNextFactor(factor, prevDelimiter) }
            factor.start(it)
        }
    }

    private fun onNextFactor(factor: Factor, prevDelimiter: Delimiter) {
        if (!reset && controller.errorStr.isNullOrEmpty()) {
            when {
                prevDelimiter == Delimiter.MULTIPLY -> number *= factor.number
                factor.number == 0.0 -> {
                    controller.errorStr = "Ошибка: деление на 0"
                    isReady = true
                    return
                }
                else -> number /= factor.number
            }
            waitOperation()
        } else isReady = true
    }

}
