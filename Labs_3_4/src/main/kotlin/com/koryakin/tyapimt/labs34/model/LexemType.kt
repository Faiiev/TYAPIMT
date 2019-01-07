package com.koryakin.tyapimt.labs34.model

enum class LexemType {
    NUMBER,     // число
    DELIMITER,  // ограничитель
    LANG_WORD,  // служебное слово
    VAR_NAME,   // идентификатор
    ;

    override fun toString(): String = when (this) {
        LexemType.NUMBER -> "число"
        LexemType.DELIMITER -> "ограничитель"
        LexemType.LANG_WORD -> "служебное слово"
        LexemType.VAR_NAME -> "идентификатор"
    }

}
