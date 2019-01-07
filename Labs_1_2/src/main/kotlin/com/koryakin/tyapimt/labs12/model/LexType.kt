package com.koryakin.tyapimt.labs12.model

enum class LexType {
    NUMBER, DELIMITER;

    override fun toString(): String = when (this) {
        LexType.NUMBER -> "число"
        LexType.DELIMITER -> "ограничитель"
    }

}
