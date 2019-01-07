package com.koryakin.tyapimt.coursework.model

import com.koryakin.tyapimt.coursework.controller.LexemesController

data class Lexeme(val type: LexemeType, val index: Int) {

    val value: String
        get() = when (type) {
            LexemeType.NUMBER -> LexemesController.numbers.elementAt(index).value
            LexemeType.DELIMITER -> Delimiter.getAt(index).value.toString()
            LexemeType.LANG_WORD -> LangWord.getAt(index).value
            LexemeType.VAR_NAME -> LexemesController.variables.elementAt(index)
        }

}
