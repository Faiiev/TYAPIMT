package com.koryakin.tyapimt.coursework.controller

import com.koryakin.tyapimt.coursework.model.*
import com.koryakin.tyapimt.coursework.model.Number
import tornadofx.*

object LexemesController {

    // Массив лексем
    val lexemes = mutableListOf<Lexeme>().observable()
    // Массив чисел
    val numbers = mutableSetOf<Number>()
    // Массив идентификаторов
    val variables = mutableSetOf<String>()

    private var numBuffer = ""
    private var varBuffer = ""
    private var isComment = false

    fun start(text: String) {
        reset()
        text.forEach { checkChar(it) }
    }

    private fun reset() {
        lexemes.clear()
        numbers.clear()
        variables.clear()
        numBuffer = ""
        varBuffer = ""
        isComment = false
    }

    private fun checkChar(char: Char) = when {
        isComment -> isComment = char != Delimiter.CURLY_BRACKETS_CLOSE.value
        char == Delimiter.CURLY_BRACKETS_OPEN.value -> isComment = true
        char.isWhitespace() -> onSpace()
        char.isDigit() -> if (varBuffer.isNotEmpty()) onLetter(char) else numBuffer += char
        Delimiter.has(char) -> onDelimiter(Delimiter.get(char))
        else -> onLetter(char)
    }

    private fun onSpace() {
        checkNumBuffer()
        checkVarBuffer()
    }

    private fun onDelimiter(delimiter: Delimiter) {
        if (numBuffer.isNotEmpty()) {
            val isDot = delimiter == Delimiter.TYPO_DOT
            val isPlusOrMinus = delimiter == Delimiter.SIGN_MINUS || delimiter == Delimiter.SIGN_PLUS
            val isNumLastE = numBuffer.last().toLowerCase() == 'e'
            if (isDot || (isPlusOrMinus && isNumLastE)) {
                numBuffer += delimiter.value
                return
            }
        }
        checkNumBuffer()
        checkVarBuffer()
        addDelimiter(delimiter)
    }

    private fun onLetter(letter: Char) {
        if (numBuffer.isNotEmpty()) numBuffer += letter
        else varBuffer += letter
    }

    private fun checkNumBuffer() {
        if (numBuffer.isEmpty()) return
        if (numBuffer.matches(Regex("^[0-9]+\$"))) addNumber(NumberType.DECIMAL)
        else {
            val type = when (numBuffer.last().toLowerCase()) {
                'b' -> NumberType.BINARY
                'o' -> NumberType.OCTAL
                'd' -> NumberType.DECIMAL
                'h' -> NumberType.HEXADECIMAL
                else -> NumberType.REAL
            }
            addNumber(type)
        }
    }

    private fun checkVarBuffer() {
        if (varBuffer.isEmpty()) return
        if (LangWord.has(varBuffer)) addLanguageWord(LangWord.get(varBuffer))
        else addVariable(varBuffer)
        varBuffer = ""
    }

    private fun addNumber(type: NumberType) {
        val number = Number(type, numBuffer)
        numbers.add(number)

        val index = numbers.indexOf(number)
        val lexeme = Lexeme(LexemeType.NUMBER, index)
        lexemes.add(lexeme)

        numBuffer = ""
    }

    private fun addDelimiter(delimiter: Delimiter) {
        val lexeme = Lexeme(LexemeType.DELIMITER, delimiter.ordinal)
        lexemes.add(lexeme)
    }

    private fun addLanguageWord(langWord: LangWord) {
        val lexeme = Lexeme(LexemeType.LANG_WORD, langWord.ordinal)
        lexemes.add(lexeme)
    }

    private fun addVariable(variable: String) {
        variables.add(variable)
        val index = variables.indexOf(variable)
        val lexeme = Lexeme(LexemeType.VAR_NAME, index)
        lexemes.add(lexeme)
    }

}
