package com.koryakin.bigram.controller

import tornadofx.*

class MainController : Controller() {

    companion object {
        private val MAIN_SYMBOLS = arrayOf(
                '\n', '\t',
                ' ', '.', ',', '!',
                '@', '#', '$', '%', '^', '&', '№',
                '*', '/', '+', '-', '=',
                '_', ':', ';', '?', '~',
                '"', '\'', '`', '\\', '|',
                '(', ')', '[', ']', '{', '}', '<', '>'
        )

        private val ADDITIONAL_SYMBOLS = arrayOf('☺', '☻', '♥', '♦', '♣', '♠', '•', '◘')

        val SYMBOLS: List<Char>

        init {
            val symbols = mutableSetOf<Char>()
            symbols.addAll('а'..'я')
            symbols.addAll('А'..'Я')
            symbols.addAll('a'..'z')
            symbols.addAll('A'..'Z')
            symbols.addAll('0'..'9')
            symbols.addAll(MAIN_SYMBOLS)

            SYMBOLS = symbols.toList()
        }
    }

    private val inputSymbols = mutableSetOf<Char>()
    private val tableSymbols = mutableSetOf<Char>()

    private fun reset() {
        inputSymbols.clear()
        tableSymbols.clear()
    }

    fun encrypt(text: String, key: String): String {
        reset()

        readKey(key)
        readText(text)

        val tableCols = Math.ceil(Math.sqrt(tableSymbols.size.toDouble())).toInt()
        val tableRows = Math.ceil(tableSymbols.size / tableCols.toDouble()).toInt()

        correctSymbolsFor(tableCols, tableRows)

        val table = getTable(tableRows, tableCols)

        val textEncrypted = StringBuilder()

        val textChunked = text.chunked(2)
        for (pair in textChunked) {
            val pos0 = getPos(pair[0], table)
            val pos1 = getPos(pair[1], table)

            when {
                pos0.second == pos1.second -> {
                    val row0 = if (pos0.first + 1 == tableRows) 0 else pos0.first + 1
                    val row1 = if (pos1.first + 1 == tableRows) 0 else pos1.first + 1
                    textEncrypted.append(table[row0][pos0.second])
                    textEncrypted.append(table[row1][pos1.second])
                }
                pos0.first == pos1.first -> {
                    val col0 = if (pos0.second + 1 == tableCols) 0 else pos0.second + 1
                    val col1 = if (pos1.second + 1 == tableCols) 0 else pos1.second + 1
                    textEncrypted.append(table[pos0.first][col0])
                    textEncrypted.append(table[pos1.first][col1])
                }
                else -> {
                    textEncrypted.append(table[pos0.first][pos1.second])
                    textEncrypted.append(table[pos1.first][pos0.second])
                }
            }
        }

        if (text.length % 2 != 0) {
            val char = text.last()
            val pos = getPos(char, table)
            val row = if (pos.first + 1 == tableRows) 0 else pos.first + 1
            textEncrypted.append(table[row][pos.second])
        }

        return textEncrypted.toString()
    }

    fun decrypt(text: String, key: String): String {
        reset()

        readKey(key)
        readText(text)

        val tableCols = Math.ceil(Math.sqrt(tableSymbols.size.toDouble())).toInt()
        val tableRows = Math.ceil(tableSymbols.size / tableCols.toDouble()).toInt()

        correctSymbolsFor(tableCols, tableRows)

        val table = getTable(tableRows, tableCols)

        val textDecrypted = StringBuilder()

        val textChunked = text.chunked(2)
        for (pair in textChunked) {
            val pos0 = getPos(pair[0], table)
            val pos1 = getPos(pair[1], table)

            when {
                pos0.second == pos1.second -> {
                    val row0 = if (pos0.first - 1 < 0) tableRows - 1 else pos0.first - 1
                    val row1 = if (pos1.first - 1 < 0) tableRows - 1 else pos1.first - 1
                    textDecrypted.append(table[row0][pos0.second])
                    textDecrypted.append(table[row1][pos1.second])
                }
                pos0.first == pos1.first -> {
                    val col0 = if (pos0.second - 1 < 0) tableCols - 1 else pos0.second - 1
                    val col1 = if (pos1.second - 1 < 0) tableCols - 1 else pos1.second - 1
                    textDecrypted.append(table[pos0.first][col0])
                    textDecrypted.append(table[pos1.first][col1])
                }
                else -> {
                    textDecrypted.append(table[pos0.first][pos1.second])
                    textDecrypted.append(table[pos1.first][pos0.second])
                }
            }
        }

        if (text.length % 2 != 0) {
            val char = text.last()
            val pos = getPos(char, table)
            val row = if (pos.first - 1 < 0) tableRows - 1 else pos.first - 1
            textDecrypted.append(table[row][pos.second])
        }

        return textDecrypted.toString()
    }

    private fun readKey(key: String) {
        tableSymbols.addAll(key.toSet())
        inputSymbols.addAll(key.toSet().filter { MAIN_SYMBOLS.contains(it) })
        addSymbolsFrom(key)
    }

    private fun readText(text: String) {
        inputSymbols.addAll(text.toSet().filter { MAIN_SYMBOLS.contains(it) })
        addSymbolsFrom(text)
        if (tableSymbols.size < 26) tableSymbols.addAll('a'..'z')
        val symbolMax = inputSymbols.map { MAIN_SYMBOLS.indexOf(it) }.max() ?: -1
        tableSymbols.addAll(MAIN_SYMBOLS.copyOfRange(0, symbolMax + 1))
    }

    private fun addSymbolsFrom(text: String) {
        var useNumbers = false
        var useRusAlphabet = false
        var useEngAlphabet = false
        var useRusAlphabetCaps = false
        var useEngAlphabetCaps = false

        for (char in text.toSet().filter { !MAIN_SYMBOLS.contains(it) }) {
            if (!useNumbers) useNumbers = char.isDigit()
            if (!useRusAlphabet) useRusAlphabet = char in 'а'..'я'
            if (!useEngAlphabet) useEngAlphabet = char in 'a'..'z'
            if (!useRusAlphabetCaps) useRusAlphabetCaps = char in 'А'..'Я'
            if (!useEngAlphabetCaps) useEngAlphabetCaps = char in 'A'..'Z'
        }

        if (useRusAlphabet) tableSymbols.addAll('а'..'я')
        if (useEngAlphabet) tableSymbols.addAll('a'..'z')
        if (useRusAlphabetCaps) tableSymbols.addAll('А'..'Я')
        if (useEngAlphabetCaps) tableSymbols.addAll('A'..'Z')
        if (useNumbers) tableSymbols.addAll('0'..'9')
    }

    private fun correctSymbolsFor(tableCols: Int, tableRows: Int) {
        var needToAdd = tableCols * tableRows - tableSymbols.size

        if (needToAdd > 0 && inputSymbols.size != MAIN_SYMBOLS.size) {
            val symbols = MAIN_SYMBOLS.filterNot { tableSymbols.contains(it) }
            for (symbol in symbols) {
                if (needToAdd == 0) break
                tableSymbols.add(symbol)
                --needToAdd
            }
        }

        if (needToAdd > 0 && !tableSymbols.contains('я')) {
            for (char in 'а'..'я') {
                if (needToAdd == 0) break
                tableSymbols.add(char)
                --needToAdd
            }
        }

        if (needToAdd > 0 && !tableSymbols.contains('Я')) {
            for (char in 'А'..'Я') {
                if (needToAdd == 0) break
                tableSymbols.add(char)
                --needToAdd
            }
        }

        if (needToAdd > 0 && !tableSymbols.contains('Z')) {
            for (char in 'A'..'Z') {
                if (needToAdd == 0) break
                tableSymbols.add(char)
                --needToAdd
            }
        }

        if (needToAdd > 0 && !tableSymbols.contains('9')) {
            for (char in '0'..'9') {
                if (needToAdd == 0) break
                tableSymbols.add(char)
                --needToAdd
            }
        }

        if (tableSymbols.size == SYMBOLS.size) tableSymbols.addAll(ADDITIONAL_SYMBOLS)
    }

    private fun getTable(rows: Int, cols: Int): List<List<Char>> {
        val table = arrayListOf<List<Char>>()
        var symbolNum = 0
        for (i in 0 until rows) {
            val row = arrayListOf<Char>()
            row.addAll(tableSymbols.toTypedArray().copyOfRange(symbolNum, symbolNum + cols))
            symbolNum += cols
            table.add(row.toList())
        }
        return table.toList()
    }

    private fun getPos(char: Char, table: List<List<Char>>): Pair<Int, Int> {
        for (row in 0 until table.size) {
            val tableRow = table[row]
            (0 until tableRow.size)
                    .filter { tableRow[it] == char }
                    .forEach { return Pair(row, it) }
        }
        return Pair(0, 0)
    }

    private fun String.chunked(size: Int): List<String> {
        val nChunks = length / size
        return (0 until nChunks).map { substring(it * size, (it + 1) * size) }
    }

}
