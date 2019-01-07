package com.koryakin.tyapimt.labs12.controller.lab2

import com.koryakin.tyapimt.labs12.controller.LexemesController
import com.koryakin.tyapimt.labs12.model.Lex
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import tornadofx.getValue
import tornadofx.setValue

class Lab2Controller : Controller() {

    var openCloseParentheses = 0

    val errorStrProperty = SimpleStringProperty(null)
    var errorStr by errorStrProperty

    val lastLexProperty = SimpleObjectProperty<Lex?>(null)
    private var lastLex: Lex? by lastLexProperty

    val resultProperty = SimpleDoubleProperty(0.0)
    private var result by resultProperty

    val resetFlagProperty = SimpleBooleanProperty(false)
    private var resetFlag by resetFlagProperty

    init {
        LexemesController.lexemes.onChange {
            if (LexemesController.lexemes.isEmpty()) reset()
            else lastLex = LexemesController.lexemes.last()
        }
        LexemesController.isLineEndedProperty.onChange { lastLex = null }
        reset()
    }

    private fun reset() {
        resetFlag = true

        result = 0.0
        errorStr = null
        lastLex = null
        openCloseParentheses = 0

        resetFlag = false

        lastLexProperty.onChangeOnce {
            val expr = Expr(this)
            expr.numberProperty.onChange { if (!expr.reset) result = it }
            expr.isReadyProperty.onChangeOnce {
                if (!expr.reset && errorStr.isNullOrEmpty() && openCloseParentheses > 0) {
                    errorStr = "Ошибка: ожидалась закрывающая скобка"
                }
            }
            expr.start(it)
        }
    }

}
