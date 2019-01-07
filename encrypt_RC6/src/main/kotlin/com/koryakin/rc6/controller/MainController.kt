package com.koryakin.rc6.controller

import tornadofx.*
import javax.xml.bind.DatatypeConverter

class MainController : Controller() {

    companion object {
        private val NON_SYMBOL = '\u0000'
    }

    fun encrypt(text: String, key: String): String {
        val keySB = StringBuilder(key)
        if (keySB.isEmpty()) keySB.append(NON_SYMBOL)
        while (keySB.length % 16 != 0) keySB.append(NON_SYMBOL)

        val keyBytes = keySB.toString().toByteArray()

        val textSB = StringBuilder(text)
        while (textSB.length % 16 != 0) textSB.append(NON_SYMBOL)

        val cipher = StringBuilder()
        (0 until textSB.length / 16)
                .map { textSB.substring(16 * it, 16 * (it + 1)) }
                .map { RC6.encrypt(it.toByteArray(), keyBytes) }
                .forEach { cipher.append(DatatypeConverter.printHexBinary(it)) }

        return cipher.toString()
    }

    fun decrypt(text: String, key: String): String {
        val keySB = StringBuilder(key)
        if (keySB.isEmpty()) keySB.append(NON_SYMBOL)
        while (keySB.length % 16 != 0) keySB.append(NON_SYMBOL)

        val keyBytes = keySB.toString().toByteArray()

        val textSB = StringBuilder(text)
        while (textSB.length % 16 != 0) textSB.append(NON_SYMBOL)

        val plain = StringBuilder()
        try {
            (0 until textSB.length / 32)
                    .map { textSB.substring(32 * it, 32 * (it + 1)) }
                    .map { DatatypeConverter.parseHexBinary(it) }
                    .map { RC6.decrypt(it, keyBytes) }
                    .forEach { plain.append(String(it)) }
        } catch (ex: Exception) {
            error("Исходный текст не может быть дешифрован", "Введенный текст не является зашифрованным по методу RC6")
        }

        return plain.toString().replace("" + NON_SYMBOL, "")
    }

}
