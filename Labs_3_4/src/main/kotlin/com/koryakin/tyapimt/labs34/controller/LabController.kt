package com.koryakin.tyapimt.labs34.controller

import com.koryakin.tyapimt.labs34.model.Delimiter
import com.koryakin.tyapimt.labs34.model.LangWord
import com.koryakin.tyapimt.labs34.model.LexemType
import com.koryakin.tyapimt.labs34.model.Lexeme
import tornadofx.*

class LabController : Controller() {

    val errors = mutableListOf<String>().observable()

    private var text = ""
    private var lexemeIndex = 0
    private val currentLexeme
        get() = LexemesController.lexemes[lexemeIndex]
    private val variables = mutableMapOf<String, LangWord?>()

    fun start(text: String) {
        this.text = text
        lexemeIndex = 0
        errors.clear()
        variables.clear()
        LexemesCreator.start(text)
        blockProgram()
    }

    private fun blockProgram() {
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_PROGRAM) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_PROGRAM.value}'")
            skipTo(LangWord.WORD_VARIABLE)
        }

        blockVar()
        blockBegin()

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.TYPO_HASH) {
            addError(lexemeIndex, "Ожидался конец программы символом '${Delimiter.TYPO_HASH.value}'")
            return
        }

        val hashIndex = text.indexOf(Delimiter.TYPO_HASH.value)
        if (hashIndex + 1 < text.trimEnd().length) {
            addError(lexemeIndex, "Неожиданные символы после конца программы")
        }
    }

    private fun blockVar() {
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_VARIABLE) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_VARIABLE.value}'")
            skipTo(Delimiter.TYPO_SEMICOLON)
            return
        }

        variableDeclaration()
        while (true) {
            ++lexemeIndex
            if (!checkLexemeIndex()) return
            if (currentLexeme.type == LexemType.DELIMITER && currentLexeme.toDelimiter() == Delimiter.TYPO_COMMA) {
                variableDeclaration()
            } else {
                --lexemeIndex
                break
            }
        }

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.TYPO_SEMICOLON) {
            addError(lexemeIndex, "Ожидался символ '${Delimiter.TYPO_SEMICOLON.value}'")
            skipTo(LangWord.WORD_BEGIN)
        }
    }

    private fun variableDeclaration() {
        val currentVars = mutableListOf<String>()

        var variableName = variableName()
        variableName?.let {
            if (variables.contains(it)) addError(lexemeIndex, "Повторное объявление переменной")
            else {
                variables.put(it, null)
                currentVars.add(it)
            }
        }
        while (true) {
            ++lexemeIndex
            if (!checkLexemeIndex()) return
            if (currentLexeme.type == LexemType.DELIMITER && currentLexeme.toDelimiter() == Delimiter.TYPO_COMMA) {
                variableName = variableName()
                variableName?.let {
                    if (variables.contains(it)) addError(lexemeIndex, "Повторное объявление переменной")
                    else {
                        variables.put(it, null)
                        currentVars.add(it)
                    }
                }
            } else {
                --lexemeIndex
                break
            }
        }

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.TYPO_COLON) {
            addError(lexemeIndex, "Ожидался символ '${Delimiter.TYPO_COLON.value}'")
            skipTo(Delimiter.TYPO_SEMICOLON)
            return
        }

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type == LexemType.LANG_WORD) {
            val lexeme = currentLexeme.toLangWord()
            if (lexeme != LangWord.TYPE_INTEGER && lexeme != LangWord.TYPE_BOOLEAN) {
                addError(lexemeIndex, "Ожидалось объявление типа переменных")
                skipTo(Delimiter.TYPO_SEMICOLON)
            } else currentVars.forEach { variables[it] = lexeme }
        } else {
            addError(lexemeIndex, "Неизвестный тип переменных")
            skipTo(Delimiter.TYPO_SEMICOLON)
        }
    }

    private fun variableName(): String? {
        ++lexemeIndex
        if (!checkLexemeIndex()) return null

        when {
            currentLexeme.type != LexemType.VAR_NAME -> addError(lexemeIndex, "Ожидалось название переменной")
            currentLexeme.toVarName()[0].isDigit() -> addError(lexemeIndex, "Название переменной не может начинаться с цифры")
            !currentLexeme.toVarName().matches(Regex("^[a-zA-Z0-9]+\$")) -> addError(lexemeIndex, "Недопустимые символы в названии переменной")
            else -> return currentLexeme.toVarName()
        }

        skipTo(Delimiter.TYPO_COLON)
        return null
    }

    private fun blockBegin() {
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_BEGIN) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_BEGIN.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        expression()
        while (true) {
            ++lexemeIndex
            if (!checkLexemeIndex()) return
            if (currentLexeme.type == LexemType.DELIMITER && currentLexeme.toDelimiter() == Delimiter.TYPO_SEMICOLON) {
                expression()
            } else {
                --lexemeIndex
                break
            }
        }

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_END) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_END.value}'")
            skipTo(LangWord.WORD_END)
        }
    }

    private fun expression() {
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type == LexemType.LANG_WORD) {
            when (currentLexeme.toLangWord()) {
                LangWord.WORD_END -> --lexemeIndex
                LangWord.WORD_BEGIN -> {
                    --lexemeIndex
                    blockBegin()
                }
                LangWord.WORD_IF -> {
                    --lexemeIndex
                    blockIf()
                }
                LangWord.WORD_WHILE -> {
                    --lexemeIndex
                    blockWhile()
                }
                LangWord.FUNCTION_READ -> {
                    --lexemeIndex
                    operationRead()
                }
                LangWord.FUNCTION_WRITE -> {
                    --lexemeIndex
                    operationWrite()
                }
                else -> {
                    addError(lexemeIndex, "Ожидалось выражение")
                    skipTo(LangWord.WORD_END)
                }
            }
        } else {
            --lexemeIndex
            variableAssignment()
        }
    }

    private fun variableAssignment() {
        val variableName = variableName()
        if (!variables.contains(variableName)) addError(lexemeIndex, "Неизвестная переменная")

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.TYPO_COLON) {
            addError(lexemeIndex, "Ожидался символ '${Delimiter.TYPO_COLON.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.SIGN_EQUALS) {
            addError(lexemeIndex, "Ожидался символ '${Delimiter.SIGN_EQUALS.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        val saveIndex = lexemeIndex
        val operationType = operation()
        val varType = variables[variableName]
        if (varType != null && varType != operationType) {
            addError(saveIndex, "Выражение не соответствует типу переменной")
        }
    }

    private fun blockIf() {
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_IF) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_IF.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        val saveIndex = lexemeIndex
        val operationType = operation()
        if (operationType != null && operationType != LangWord.TYPE_BOOLEAN) {
            addError(saveIndex, "Ожидалось логическое выражение")
        }
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_THEN) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_THEN.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        expression()
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_ELSE) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_ELSE.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        expression()
    }

    private fun blockWhile() {
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_WHILE) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_WHILE.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        val saveIndex = lexemeIndex
        val operationType = operation()
        if (operationType != null && operationType != LangWord.TYPE_BOOLEAN) {
            addError(saveIndex, "Ожидалось логическое выражение")
        }
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.WORD_DO) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.WORD_DO.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        expression()
    }

    private fun operationRead() {
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.FUNCTION_READ) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.FUNCTION_READ.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.PARENTHESES_OPEN) {
            addError(lexemeIndex, "Ожидалося символ '${Delimiter.PARENTHESES_OPEN.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        val variableName = variableName()
        if (!variables.contains(variableName)) addError(lexemeIndex, "Неизвестная переменная")

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.PARENTHESES_CLOSE) {
            addError(lexemeIndex, "Ожидалося символ '${Delimiter.PARENTHESES_CLOSE.value}'")
            skipTo(LangWord.WORD_END)
        }
    }

    private fun operationWrite() {
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.LANG_WORD || currentLexeme.toLangWord() != LangWord.FUNCTION_WRITE) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${LangWord.FUNCTION_WRITE.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.PARENTHESES_OPEN) {
            addError(lexemeIndex, "Ожидалося символ '${Delimiter.PARENTHESES_OPEN.value}'")
            skipTo(LangWord.WORD_END)
            return
        }

        operation()
        ++lexemeIndex
        if (!checkLexemeIndex()) return
        if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.PARENTHESES_CLOSE) {
            addError(lexemeIndex, "Ожидалося символ '${Delimiter.PARENTHESES_CLOSE.value}'")
            skipTo(LangWord.WORD_END)
        }
    }

    private fun operation(): LangWord? {
        val type = operationPlusMinusOr()

        ++lexemeIndex
        if (!checkLexemeIndex()) return type
        val saveIndex = lexemeIndex
        val saveOperation = currentLexeme
        when (currentLexeme.type) {
            LexemType.DELIMITER -> {
                val delimiter = currentLexeme.toDelimiter()
                when (delimiter) {
                    Delimiter.SIGN_EQUALS,
                    Delimiter.SIGN_LESS_THAN,
                    Delimiter.SIGN_GREATER_THAN -> {
                        // it's okay
                    }
                    Delimiter.SIGN_EXCLAMATION_MARK -> {
                        ++lexemeIndex
                        if (!checkLexemeIndex()) return null
                        if (currentLexeme.type != LexemType.DELIMITER && currentLexeme.toDelimiter() != Delimiter.SIGN_EQUALS) {
                            addError(lexemeIndex, "Ожидался знак операции '${Delimiter.SIGN_EQUALS.value}'")
                            skipTo(LangWord.WORD_END)
                            return type
                        }
                    }
                    else -> {
                        --lexemeIndex
                        return type
                    }
                }
            }
            LexemType.LANG_WORD -> {
                --lexemeIndex
                return type
            }
            else -> {
                addError(lexemeIndex, "Ожидалась операция")
                skipTo(LangWord.WORD_END)
                return type
            }
        }

        val saveIndex2 = lexemeIndex
        val nextType = operationPlusMinusOr()
        if (nextType != type) {
            addError(saveIndex2, "Ожидалось ${if (type == LangWord.TYPE_INTEGER) "числовое" else "логическое"} значение")
        }
        if (type != null && nextType != null && type != nextType) {
            addError(saveIndex, "Невозможно выполнить операцию '${saveOperation.value}' между данными типами операндов")
        }

        return LangWord.TYPE_BOOLEAN
    }

    private fun operationPlusMinusOr(): LangWord? {
        val type = operationMulDivAnd()
        val expectedNextType: LangWord

        ++lexemeIndex
        if (!checkLexemeIndex()) return type
        val saveIndex = lexemeIndex
        val saveOperation = currentLexeme
        when (currentLexeme.type) {
            LexemType.DELIMITER -> {
                val delimiter = currentLexeme.toDelimiter()
                when (delimiter) {
                    Delimiter.SIGN_PLUS,
                    Delimiter.SIGN_MINUS -> {
                        expectedNextType = LangWord.TYPE_INTEGER
                    }
                    else -> {
                        --lexemeIndex
                        return type
                    }
                }
            }
            LexemType.LANG_WORD -> {
                val langWord = currentLexeme.toLangWord()
                when (langWord) {
                    LangWord.OPERATION_OR -> {
                        expectedNextType = LangWord.TYPE_BOOLEAN
                    }
                    else -> {
                        --lexemeIndex
                        return type
                    }
                }
            }
            else -> {
                addError(lexemeIndex, "Ожидалась операция")
                skipTo(LangWord.WORD_END)
                return type
            }
        }

        val saveIndex2 = lexemeIndex
        val nextType = operationMulDivAnd()
        if (nextType != expectedNextType) {
            addError(saveIndex2, "Ожидалось ${if (expectedNextType == LangWord.TYPE_INTEGER) "числовое" else "логическое"} значение")
        }
        if (type != null && nextType != null && type != nextType) {
            addError(saveIndex, "Невозможно выполнить операцию '${saveOperation.value}' между данными типами операндов")
        }

        return expectedNextType
    }

    private fun operationMulDivAnd(): LangWord? {
        val type = operationMember()
        val expectedNextType: LangWord

        ++lexemeIndex
        if (!checkLexemeIndex()) return type
        val saveIndex = lexemeIndex
        val saveOperation = currentLexeme
        when (currentLexeme.type) {
            LexemType.DELIMITER -> {
                val delimiter = currentLexeme.toDelimiter()
                when (delimiter) {
                    Delimiter.SIGN_MULTIPLY,
                    Delimiter.SIGN_DIVIDE -> {
                        expectedNextType = LangWord.TYPE_INTEGER
                    }
                    else -> {
                        --lexemeIndex
                        return type
                    }
                }
            }
            LexemType.LANG_WORD -> {
                val langName = currentLexeme.toLangWord()
                when (langName) {
                    LangWord.OPERATION_AND -> {
                        expectedNextType = LangWord.TYPE_BOOLEAN
                    }
                    else -> {
                        --lexemeIndex
                        return type
                    }
                }
            }
            else -> {
                addError(lexemeIndex, "Ожидалась операция")
                skipTo(LangWord.WORD_END)
                return type
            }
        }

        val saveIndex2 = lexemeIndex
        val nextType = operationMember()
        if (nextType != expectedNextType) {
            addError(saveIndex2, "Ожидалось ${if (expectedNextType == LangWord.TYPE_INTEGER) "числовое" else "логическое"} значение")
        }
        if (type != null && nextType != null && type != nextType) {
            addError(saveIndex, "Невозможно выполнить операцию '${saveOperation.value}' между данными типами операндов")
        }

        return expectedNextType
    }

    private fun operationMember(): LangWord? {
        ++lexemeIndex
        if (!checkLexemeIndex()) return null
        when (currentLexeme.type) {
            LexemType.LANG_WORD -> {
                val langWord = currentLexeme.toLangWord()
                if (langWord == LangWord.OPERATION_NOT) {
                    val saveIndex = lexemeIndex
                    val type = operationMember()
                    if (type != LangWord.TYPE_BOOLEAN) {
                        addError(saveIndex, "Ожидалось логическое значение после оператора '${LangWord.OPERATION_NOT.value}'")
                    }
                } else if (langWord != LangWord.VALUE_TRUE && langWord != LangWord.VALUE_FALSE) {
                    addError(lexemeIndex, "Ожидалось значение")
                    skipTo(LangWord.WORD_END)
                }
                return LangWord.TYPE_BOOLEAN
            }
            LexemType.VAR_NAME -> {
                --lexemeIndex
                val variableName = variableName()
                if (!variables.contains(variableName)) addError(lexemeIndex, "Неизвестная переменная")
                return variables[variableName]
            }
            LexemType.DELIMITER -> {
                val delimiter = currentLexeme.toDelimiter()
                if (delimiter != Delimiter.PARENTHESES_OPEN) {
                    addError(lexemeIndex, "Ожидался символ '${Delimiter.PARENTHESES_OPEN.value}'")
                    skipTo(LangWord.WORD_END)
                    return null
                } else {
                    val type = operation()
                    ++lexemeIndex
                    if (!checkLexemeIndex()) return null
                    if (currentLexeme.type != LexemType.DELIMITER || currentLexeme.toDelimiter() != Delimiter.PARENTHESES_CLOSE) {
                        addError(lexemeIndex, "Ожидался символ '${Delimiter.PARENTHESES_CLOSE.value}'")
                        skipTo(LangWord.WORD_END)
                    }
                    return type
                }
            }
            LexemType.NUMBER -> return LangWord.TYPE_INTEGER
        }
    }

    private fun skipTo(langWord: LangWord) {
        for (i in lexemeIndex until LexemesController.lexemes.size) {
            lexemeIndex = i - 1
            val lexeme = LexemesController.lexemes.elementAt(i)
            if (lexeme.type == LexemType.LANG_WORD && lexeme.toLangWord() == langWord) break
        }
    }

    private fun skipTo(delimiter: Delimiter) {
        for (i in lexemeIndex until LexemesController.lexemes.size) {
            lexemeIndex = i - 1
            val lexeme = LexemesController.lexemes.elementAt(i)
            if (lexeme.type == LexemType.DELIMITER && lexeme.toDelimiter() == delimiter) break
        }
    }

    private fun checkLexemeIndex(): Boolean = lexemeIndex < LexemesController.lexemes.size

    private fun addError(lexemeIndex: Int, error: String) {
        val lexemesLength = (0 until lexemeIndex)
                .map { LexemesController.lexemes.elementAt(it) }
                .sumBy {
                    when (it.type) {
                        LexemType.NUMBER -> it.toNumber().toString().length
                        LexemType.DELIMITER -> it.toDelimiter().value.toString().length
                        LexemType.LANG_WORD -> it.toLangWord().value.length
                        LexemType.VAR_NAME -> it.toVarName().length
                    }
                }

        var textPos = 0
        var textLine = 1
        var textLinePos = 0
        var textLexemesPos = -1

        while (lexemesLength != textLexemesPos) {
            when {
                text[textPos] == '\n' -> {
                    ++textLine
                    textLinePos = 0
                }
                text[textPos].isWhitespace() -> ++textLinePos
                else -> {
                    ++textLinePos
                    ++textLexemesPos
                }
            }
            ++textPos
        }

        errors.add("$textLine : $textLinePos \t '${LexemesController.lexemes.elementAt(lexemeIndex).value}' \t $error")
    }

    private fun Lexeme.toNumber(): Int = LexemesController.numbers.elementAt(index)
    private fun Lexeme.toDelimiter(): Delimiter = Delimiter.getAt(index)
    private fun Lexeme.toLangWord(): LangWord = LangWord.getAt(index)
    private fun Lexeme.toVarName(): String = LexemesController.variables.elementAt(index)

}
