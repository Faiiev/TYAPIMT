package com.koryakin.tyapimt.labs12.controller.lab2

import com.koryakin.tyapimt.labs12.model.Delimiter
import com.koryakin.tyapimt.labs12.model.Lex
import tornadofx.*

class Expr(controller: Lab2Controller) : BaseClass(controller) {

    override fun start(lex: Lex?) {
        if (reset) {
            isReady = true
            return
        }
        val term = Term(controller)
        term.isReadyProperty.onChangeOnce {
            if (controller.errorStr.isNullOrEmpty()) {
                number = term.number
                waitNextTerm(term.lastLex)
            } else isReady = true
        }
        term.start(lex)
    }

    private fun waitNextTerm(lastLex: Lex?) {
        if (lastLex == null || reset) isReady = true
        else {
            val prevDelimiter = Delimiter.getByIndex(lastLex.index)
            controller.lastLexProperty.onChangeOnce {
                if (reset) {
                    isReady = true
                    return@onChangeOnce
                }
                if (it == null) {
                    if (prevDelimiter != Delimiter.PARENTHESES_CLOSE) {
                        controller.errorStr = "Ошибка: неожиданное завершение строки"
                    }
                    isReady = true
                    return@onChangeOnce
                }
                val term = Term(controller)
                term.isReadyProperty.onChangeOnce { onNextTerm(term, prevDelimiter) }
                term.start(it)
            }
        }
    }

    private fun onNextTerm(term: Term, prevDelimiter: Delimiter) {
        if (!reset && controller.errorStr.isNullOrEmpty()) {
            if (prevDelimiter == Delimiter.PLUS) number += term.number else number -= term.number
            if (term.afterClose) isReady = true
            else waitNextTerm(term.lastLex)
        } else isReady = true
    }

}
