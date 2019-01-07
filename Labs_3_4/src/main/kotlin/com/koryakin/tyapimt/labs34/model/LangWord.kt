package com.koryakin.tyapimt.labs34.model

enum class LangWord(val value: String) {
    WORD_PROGRAM("program"),
    WORD_VARIABLE("var"),

    WORD_BEGIN("begin"),
    WORD_END("end"),

    WORD_IF("if"),
    WORD_THEN("then"),
    WORD_ELSE("else"),

    WORD_WHILE("while"),
    WORD_DO("do"),

    OPERATION_OR("or"),
    OPERATION_AND("and"),
    OPERATION_NOT("not"),

    TYPE_INTEGER("int"),
    TYPE_BOOLEAN("bool"),

    VALUE_TRUE("true"),
    VALUE_FALSE("false"),

    FUNCTION_READ("read"),
    FUNCTION_WRITE("write"),
    ;

    companion object {
        fun has(value: String): Boolean = values().any { it.value == value }
        fun get(value: String): LangWord = values().first { it.value == value }
        fun getAt(index: Int): LangWord = values().first { it.ordinal == index }
    }

}
