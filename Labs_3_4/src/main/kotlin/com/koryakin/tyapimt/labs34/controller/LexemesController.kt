package com.koryakin.tyapimt.labs34.controller

import com.koryakin.tyapimt.labs34.model.Delimiter
import com.koryakin.tyapimt.labs34.model.LangWord
import com.koryakin.tyapimt.labs34.model.Lexeme
import com.koryakin.tyapimt.labs34.model.LexemType
import tornadofx.*

object LexemesController {

    // массив лексем
    val lexemes = mutableListOf<Lexeme>().observable()
    // массив чисел
    val numbers = mutableSetOf<Int>()
    // массив идентификаторов
    val variables = mutableSetOf<String>()

    fun reset() {
        lexemes.clear()
        numbers.clear()
        variables.clear()
    }

    fun addNumber(number: Int) {
        numbers.add(number)
        val index = numbers.indexOf(number)
        val lexeme = Lexeme(LexemType.NUMBER, index)
        lexemes.add(lexeme)
    }

    fun addDelimiter(delimiter: Delimiter) {
        val lexeme = Lexeme(LexemType.DELIMITER, delimiter.ordinal)
        lexemes.add(lexeme)
    }

    fun addLanguageWord(langWord: LangWord) {
        val lexeme = Lexeme(LexemType.LANG_WORD, langWord.ordinal)
        lexemes.add(lexeme)
    }

    fun addVariable(variable: String) {
        variables.add(variable)
        val index = variables.indexOf(variable)
        val lexeme = Lexeme(LexemType.VAR_NAME, index)
        lexemes.add(lexeme)
    }

}
