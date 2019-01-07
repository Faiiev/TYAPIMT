package com.koryakin.tyapimt.labs12.controller

import com.koryakin.tyapimt.labs12.model.Delimiter
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import tornadofx.getValue
import tornadofx.setValue

class Lab1Controller : Controller() {

    val incorrectCharProperty = SimpleObjectProperty<Char?>(null)
    var incorrectChar: Char? by incorrectCharProperty

    private var numBuffer = -1

    fun reset() {
        LexemesController.reset()
        incorrectChar = null
        numBuffer = -1
    }

    fun nextChar(char: Char) {
        when {
            char == LexemesController.endLineChar -> onEndLine()
            char.isDigit() -> onDigit(Character.getNumericValue(char))
            Delimiter.existsValue(char) -> onDelimiter(Delimiter.getByValue(char))
            char == ' ' -> if (numBuffer >= 0) addNumber(numBuffer)
            else -> incorrectChar = char
        }
    }

    private fun onEndLine() {
        if (numBuffer >= 0) addNumber(numBuffer)
        LexemesController.isLineEnded = true
    }

    private fun onDigit(digit: Int) {
        numBuffer = if (numBuffer < 0) digit else numBuffer * 10 + digit
    }

    private fun onDelimiter(delimiter: Delimiter) {
        if (numBuffer >= 0) addNumber(numBuffer)
        LexemesController.addDelimiter(delimiter)
    }

    private fun addNumber(number: Int) {
        LexemesController.addNumber(number)
        numBuffer = -1
    }

}
