package com.koryakin.tyapimt.coursework.model

enum class Delimiter(val value: Char) {
    TYPO_DOT('.'),
    TYPO_COMMA(','),
    TYPO_COLON(':'),
    TYPO_SEMICOLON(';'),

    SIGN_PLUS('+'),
    SIGN_MINUS('-'),
    SIGN_DIVIDE('/'),
    SIGN_MULTIPLY('*'),

    SIGN_EQUALS('='),
    SIGN_LESS_THAN('<'),
    SIGN_GREATER_THAN('>'),

    PARENTHESES_OPEN('('),
    PARENTHESES_CLOSE(')'),
    CURLY_BRACKETS_OPEN('{'),
    CURLY_BRACKETS_CLOSE('}'),
    ;

    companion object {
        fun has(value: Char): Boolean = values().any { it.value == value }
        fun get(value: Char): Delimiter = values().first { it.value == value }
        fun getAt(index: Int): Delimiter = values().first { it.ordinal == index }
    }

}
