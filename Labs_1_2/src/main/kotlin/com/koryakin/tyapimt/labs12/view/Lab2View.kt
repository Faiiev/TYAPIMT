package com.koryakin.tyapimt.labs12.view

import com.koryakin.tyapimt.labs12.controller.lab2.Lab2Controller
import javafx.scene.control.Label
import javafx.scene.text.FontWeight
import tornadofx.*

class Lab2View : View() {

    private val controller: Lab2Controller by inject()
    private var outputLabel: Label by singleAssign()
    private val outputResult: String
        get() = "Ошибок не найдено. Результат: ${controller.resultProperty.value}"

    override val root = vbox(8.px.value) {
        separator()

        outputLabel = label(outputResult) {
            style {
                fontSize = 16.px
                fontWeight = FontWeight.BOLD
                textFill = c("#008700")
            }
        }
    }

    init {
        controller.errorStrProperty.onChange {
            if (it == null) {
                outputLabel.text = outputResult
                outputLabel.textFill = c("#008700")
            } else {
                outputLabel.text = it
                outputLabel.textFill = c("#DD0000")
            }
        }

        controller.resultProperty.onChange { outputLabel.text = outputResult }
    }

}
