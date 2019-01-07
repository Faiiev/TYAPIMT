package com.koryakin.tyapimt.labs34.model

import com.koryakin.tyapimt.labs34.controller.LexemesController

data class Lexeme(val type: LexemType, val index: Int) {

    val value: String
        get() = when (type) {
            LexemType.NUMBER -> LexemesController.numbers.elementAt(index).toString()
            LexemType.DELIMITER -> Delimiter.getAt(index).value.toString()
            LexemType.LANG_WORD -> LangWord.getAt(index).value
            LexemType.VAR_NAME -> LexemesController.variables.elementAt(index)
        }

}
