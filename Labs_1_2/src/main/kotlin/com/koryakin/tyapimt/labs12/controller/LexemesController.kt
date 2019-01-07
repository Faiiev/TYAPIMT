package com.koryakin.tyapimt.labs12.controller

import com.koryakin.tyapimt.labs12.model.Delimiter
import com.koryakin.tyapimt.labs12.model.Lex
import com.koryakin.tyapimt.labs12.model.LexType
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*

object LexemesController {

    // символ конца строки
    val endLineChar = '#'

    val isLineEndedProperty = SimpleBooleanProperty(false)
    var isLineEnded by isLineEndedProperty

    // массив чисел
    val numbers = mutableSetOf<Int>()
    // массив лексем
    val lexemes = mutableListOf<Lex>().observable()

    fun reset() {
        isLineEnded = false
        lexemes.clear()
        numbers.clear()
    }

    fun addNumber(number: Int) {
        numbers.add(number)
        val index = numbers.indexOf(number)
        val lex = Lex(lexemes.size, LexType.NUMBER, index)
        lexemes.add(lex)
    }

    fun addDelimiter(delimiter: Delimiter) {
        val lex = Lex(lexemes.size, LexType.DELIMITER, delimiter.ordinal)
        lexemes.add(lex)
    }

}
