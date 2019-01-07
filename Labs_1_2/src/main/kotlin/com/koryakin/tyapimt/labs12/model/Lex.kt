package com.koryakin.tyapimt.labs12.model

import com.koryakin.tyapimt.labs12.controller.LexemesController

data class Lex(val id: Int, val type: LexType, val index: Int) {

    val value: String
        get() = when (type) {
            LexType.NUMBER -> LexemesController.numbers.elementAt(index).toString()
            LexType.DELIMITER -> Delimiter.getByIndex(index).value.toString()
        }

}
