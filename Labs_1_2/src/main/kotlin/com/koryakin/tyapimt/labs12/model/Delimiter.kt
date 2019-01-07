package com.koryakin.tyapimt.labs12.model

enum class Delimiter(val value: Char) {
    PLUS('+'), MINUS('-'),
    MULTIPLY('*'), DIVIDE('/'),
    PARENTHESES_OPEN('('), PARENTHESES_CLOSE(')');

    companion object {
        fun existsValue(value: Char): Boolean = values().any { it.value == value }
        fun getByValue(value: Char): Delimiter = values().first { it.value == value }
        fun getByIndex(index: Int): Delimiter = values().first { it.ordinal == index }
    }

}
