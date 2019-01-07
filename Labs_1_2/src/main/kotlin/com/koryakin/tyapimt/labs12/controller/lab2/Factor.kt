package com.koryakin.tyapimt.labs12.controller.lab2

import com.koryakin.tyapimt.labs12.controller.LexemesController
import com.koryakin.tyapimt.labs12.model.Delimiter
import com.koryakin.tyapimt.labs12.model.Lex
import com.koryakin.tyapimt.labs12.model.LexType
import tornadofx.*

class Factor(controller: Lab2Controller) : BaseClass(controller) {

    override fun start(lex: Lex?) {
        if (reset || lex == null) isReady = true
        else when (lex.type) {
            LexType.NUMBER -> {
                number = LexemesController.numbers.elementAt(lex.index).toDouble()
                isReady = true
            }
            LexType.DELIMITER -> checkOpen(Delimiter.getByIndex(lex.index))
        }
    }

    private fun checkOpen(delimiter: Delimiter) {
        when {
            reset -> isReady = true
            delimiter == Delimiter.PARENTHESES_CLOSE -> {
                controller.openCloseParentheses--
                if (controller.openCloseParentheses < 0) controller.errorStr = "Ошибка: лишняя закрывающая скобка"
                isReady = true
            }
            delimiter == Delimiter.PARENTHESES_OPEN -> {
                controller.openCloseParentheses++
                waitExpr()
            }
            else -> {
                controller.errorStr = "Ошибка: ожидалась открывающая скобка"
                isReady = true
            }
        }
    }

    private fun waitExpr() {
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
            val expr = Expr(controller)
            expr.isReadyProperty.onChangeOnce { onNewExpr(expr) }
            expr.start(it)
        }
    }

    private fun onNewExpr(expr: Expr) {
        if (controller.errorStr.isNullOrEmpty()) {
            number = expr.numberProperty.value
        }
        isReady = true
    }

}
