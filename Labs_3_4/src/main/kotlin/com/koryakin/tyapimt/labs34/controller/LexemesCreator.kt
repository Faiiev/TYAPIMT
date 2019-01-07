package com.koryakin.tyapimt.labs34.controller

import com.koryakin.tyapimt.labs34.model.Delimiter
import com.koryakin.tyapimt.labs34.model.LangWord

object LexemesCreator {

    private var numBuffer = -1
    private var varBuffer = ""

    fun start(text: String) {
        LexemesController.reset()

        numBuffer = -1
        varBuffer = ""

        text.forEach { char ->
            when {
                char.isWhitespace() -> onSpace()
                char.isDigit() -> onDigit(char)
                Delimiter.has(char) -> onDelimiter(Delimiter.get(char))
                else -> onLetter(char)
            }
        }
    }

    private fun onSpace() {
        checkNumBuffer()
        checkVarBuffer()
    }

    private fun onDigit(char: Char) {
        if (varBuffer.isNotEmpty()) {
            onLetter(char)
        } else {
            val digit = Character.getNumericValue(char)
            numBuffer = if (numBuffer < 0) digit else numBuffer * 10 + digit
        }
    }

    private fun onLetter(letter: Char) {
        if (numBuffer >= 0) {
            varBuffer = numBuffer.toString()
            numBuffer = -1
        }
        varBuffer += letter
    }

    private fun onDelimiter(delimiter: Delimiter) {
        checkNumBuffer()
        checkVarBuffer()
        LexemesController.addDelimiter(delimiter)
    }

    private fun checkNumBuffer() {
        if (numBuffer < 0) return

        LexemesController.addNumber(numBuffer)
        numBuffer = -1
    }

    private fun checkVarBuffer() {
        if (varBuffer.isEmpty()) return

        if (LangWord.has(varBuffer)) {
            val langWord = LangWord.get(varBuffer)
            LexemesController.addLanguageWord(langWord)
        } else {
            LexemesController.addVariable(varBuffer)
        }

        varBuffer = ""
    }

}
