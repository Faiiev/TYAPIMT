package com.koryakin.tyapimt.labs34.model

enum class Delimiter(val value: Char) {
    TYPO_HASH('#'),
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
    SIGN_EXCLAMATION_MARK('!'),

    PARENTHESES_OPEN('('),
    PARENTHESES_CLOSE(')'),
    ;

    companion object {
        fun has(value: Char): Boolean = values().any { it.value == value }
        fun get(value: Char): Delimiter = values().first { it.value == value }
        fun getAt(index: Int): Delimiter = values().first { it.ordinal == index }
    }

}
