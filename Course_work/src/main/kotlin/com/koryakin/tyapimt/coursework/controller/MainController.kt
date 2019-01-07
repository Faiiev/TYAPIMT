package com.koryakin.tyapimt.coursework.controller

import com.koryakin.tyapimt.coursework.model.*
import com.koryakin.tyapimt.coursework.model.Number
import tornadofx.*

class MainController : Controller() {

    // Список найденных ошибок
    val errors = mutableListOf<String>().observable()

    private lateinit var text: String
    private var lexemeIndex = 0
    private val currentLexeme
        get() = LexemesController.lexemes[lexemeIndex]
    private val variables = mutableMapOf<String, LangWord?>()

    /**
     * Запускает разбор текста программы
     * @param text текст программы
     */
    fun start(text: String) {
        this.text = text
        lexemeIndex = 0
        errors.clear()
        variables.clear()
        LexemesController.start(text)
        blockProgram()
    }

    private fun blockProgram() {
        addErrorIfNot(LangWord.WORD_PROGRAM, LangWord.WORD_VARIABLE)

        blockVar()
        blockBegin()

        ++lexemeIndex
        if (!isDelimiter(Delimiter.TYPO_DOT)) {
            addError(lexemeIndex, "Ожидался конец программы символом '${Delimiter.TYPO_DOT.value}'")
        } else {
            val dotIndex = text.lastIndexOf(Delimiter.TYPO_DOT.value)
            val isError = dotIndex + 1 < text.trimEnd().length
            if (isError) addError(lexemeIndex, "Неожиданные символы после конца программы")
        }
    }

    private fun blockVar() {
        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_VARIABLE, LangWord.WORD_BEGIN)) return

        variableDeclaration()
        while (true) {
            if (isNextDelimiter(Delimiter.TYPO_SEMICOLON)) {
                ++lexemeIndex
                variableDeclaration()
            } else break
        }
    }

    private fun variableDeclaration() {
        val currentVars = mutableListOf<String>()

        ++lexemeIndex
        if (!checkLexemeIndex()) {
            addError(lexemeIndex, "Ожидалось объявление типа переменных")
            skipTo(LangWord.WORD_BEGIN)
            return
        }

        val type: LangWord
        if (currentLexeme.type == LexemeType.LANG_WORD) {
            type = currentLexeme.toLangWord()
            if (type != LangWord.TYPE_INTEGER && type != LangWord.TYPE_REAL && type != LangWord.TYPE_BOOLEAN) {
                addError(lexemeIndex, "Ожидалось объявление типа переменных")
                skipTo(LangWord.WORD_BEGIN)
                return
            }
        } else {
            addError(lexemeIndex, "Неизвестный тип переменных")
            skipTo(LangWord.WORD_BEGIN)
            return
        }

        fun getNextVariable() {
            val variableName = variableName() ?: return
            if (!variables.contains(variableName)) {
                variables.put(variableName, null)
                currentVars.add(variableName)
            } else addError(lexemeIndex, "Повторное объявление переменной")
        }

        getNextVariable()
        while (true) {
            if (isNextDelimiter(Delimiter.TYPO_COMMA)) {
                ++lexemeIndex
                getNextVariable()
            } else break
        }

        currentVars.forEach { variables[it] = type }
    }

    private fun variableName(): String? {
        ++lexemeIndex
        when {
            !checkLexemeIndex() || currentLexeme.type != LexemeType.VAR_NAME -> addError(lexemeIndex, "Ожидалось название переменной")
            currentLexeme.toVarName()[0].isDigit() -> addError(lexemeIndex, "Название переменной не может начинаться с цифры")
            !currentLexeme.toVarName().matches(Regex("^[a-zA-Z0-9]+\$")) -> addError(lexemeIndex, "Недопустимые символы в названии переменной")
            else -> return currentLexeme.toVarName()
        }
        skipTo(LangWord.WORD_BEGIN)
        return null
    }

    private fun blockBegin() {
        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_BEGIN, LangWord.WORD_END)) return

        expression()
        while (true) {
            if (isNextDelimiter(Delimiter.TYPO_SEMICOLON)) {
                ++lexemeIndex
                expression()
            } else break
        }

        ++lexemeIndex
        addErrorIfNot(LangWord.WORD_END, LangWord.WORD_END)
    }

    private fun expression() {
        ++lexemeIndex
        if (!checkLexemeIndex()) {
            addError(lexemeIndex, "Ожидалось выражение")
            skipTo(LangWord.WORD_END)
            return
        }

        if (currentLexeme.type == LexemeType.LANG_WORD) {
            val lexeme = currentLexeme.toLangWord()
            --lexemeIndex
            when (lexeme) {
                LangWord.WORD_END -> {
                    // it's okay
                }
                LangWord.WORD_BEGIN -> blockBegin()
                LangWord.WORD_IF -> blockIf()
                LangWord.WORD_FOR -> blockFor()
                LangWord.WORD_WHILE -> blockWhile()
                LangWord.FUNCTION_READ -> operationRead()
                LangWord.FUNCTION_WRITE -> operationWrite()
                else -> {
                    addError(++lexemeIndex, "Ожидалось выражение")
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
        if (addErrorIfNot(LangWord.WORD_AS, LangWord.WORD_END)) return

        val saveIndex = lexemeIndex
        val operationType = operation()
        val varType = variables[variableName]
        if (varType != null && varType != operationType) {
            addError(saveIndex, "Выражение не соответствует типу переменной")
        }
    }

    private fun blockIf() {
        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_IF, LangWord.WORD_END)) return

        val saveIndex = lexemeIndex
        val operationType = operation()
        if (operationType != null && operationType != LangWord.TYPE_BOOLEAN) {
            addError(saveIndex, "Ожидалось логическое выражение")
        }

        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_THEN, LangWord.WORD_END)) return

        expression()
        ++lexemeIndex
        if (isLangWord(LangWord.WORD_ELSE)) expression()
        else --lexemeIndex
    }

    private fun blockFor() {
        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_FOR, LangWord.WORD_END)) return

        variableAssignment()
        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_TO, LangWord.WORD_END)) return

        operation()
        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_DO, LangWord.WORD_END)) return

        expression()
    }

    private fun blockWhile() {
        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_WHILE, LangWord.WORD_END)) return

        val saveIndex = lexemeIndex
        val operationType = operation()
        if (operationType != null && operationType != LangWord.TYPE_BOOLEAN) {
            addError(saveIndex, "Ожидалось логическое выражение")
        }

        ++lexemeIndex
        if (addErrorIfNot(LangWord.WORD_DO, LangWord.WORD_END)) return

        expression()
    }

    private fun operationRead() {
        ++lexemeIndex
        if (addErrorIfNot(LangWord.FUNCTION_READ, LangWord.WORD_END)) return

        ++lexemeIndex
        if (addErrorIfNot(Delimiter.PARENTHESES_OPEN, LangWord.WORD_END)) return

        fun getNextVariable() {
            val variableName = variableName() ?: return
            if (!variables.contains(variableName)) addError(lexemeIndex, "Неизвестная переменная")
        }

        getNextVariable()
        while (true) {
            if (isNextDelimiter(Delimiter.TYPO_COMMA)) {
                ++lexemeIndex
                getNextVariable()
            } else break
        }

        ++lexemeIndex
        addErrorIfNot(Delimiter.PARENTHESES_CLOSE, LangWord.WORD_END)
    }

    private fun operationWrite() {
        ++lexemeIndex
        if (addErrorIfNot(LangWord.FUNCTION_WRITE, LangWord.WORD_END)) return

        ++lexemeIndex
        if (addErrorIfNot(Delimiter.PARENTHESES_OPEN, LangWord.WORD_END)) return

        operation()
        while (true) {
            if (isNextDelimiter(Delimiter.TYPO_COMMA)) {
                ++lexemeIndex
                operation()
            } else break
        }

        ++lexemeIndex
        addErrorIfNot(Delimiter.PARENTHESES_CLOSE, LangWord.WORD_END)
    }

    private fun operation(): LangWord? {
        val type = operationPlusMinusOr()

        ++lexemeIndex
        if (!checkLexemeIndex()) return type
        val saveIndex = lexemeIndex
        val saveOperation = currentLexeme
        when (currentLexeme.type) {
            LexemeType.DELIMITER -> {
                val delimiter = currentLexeme.toDelimiter()
                when (delimiter) {
                    Delimiter.SIGN_EQUALS -> {
                        // it's okay
                    }
                    Delimiter.SIGN_GREATER_THAN -> {
                        if (isNextDelimiter(Delimiter.SIGN_EQUALS)) ++lexemeIndex
                        // it's okay
                    }
                    Delimiter.SIGN_LESS_THAN -> {
                        if (isNextDelimiter(Delimiter.SIGN_EQUALS) || isNextDelimiter(Delimiter.SIGN_GREATER_THAN)) ++lexemeIndex
                        // it's okay
                    }
                    else -> {
                        --lexemeIndex
                        return type
                    }
                }
            }
            LexemeType.LANG_WORD -> {
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
            LexemeType.DELIMITER -> {
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
            LexemeType.LANG_WORD -> {
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
            LexemeType.DELIMITER -> {
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
            LexemeType.LANG_WORD -> {
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
            LexemeType.LANG_WORD -> {
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
            LexemeType.VAR_NAME -> {
                --lexemeIndex
                val variableName = variableName()
                if (!variables.contains(variableName)) addError(lexemeIndex, "Неизвестная переменная")
                return variables[variableName]
            }
            LexemeType.DELIMITER -> {
                if (addErrorIfNot(Delimiter.PARENTHESES_OPEN, LangWord.WORD_END)) return null
                val type = operation()
                ++lexemeIndex
                addErrorIfNot(Delimiter.PARENTHESES_CLOSE, LangWord.WORD_END)
                return type
            }
            LexemeType.NUMBER -> {
                val number = currentLexeme.toNumber()
                val regex = when (number.type) {
                    NumberType.BINARY -> Regex("^[0-1]+[Bb]\$")
                    NumberType.OCTAL -> Regex("^[0-7]+[Oo]\$")
                    NumberType.DECIMAL -> Regex("^[0-9]+[Dd]?\$")
                    NumberType.HEXADECIMAL -> Regex("^[a-fA-F0-9]+[Hh]\$")
                    NumberType.REAL -> Regex("^([0-9]+)?[.]?[0-9]+[Ee][+-][0-9]+\$")
                }
                if (number.value.matches(regex)) return if (number.type == NumberType.REAL) LangWord.TYPE_REAL else LangWord.TYPE_INTEGER
                else addError(lexemeIndex, "Неверная запись числа")
                return null
            }
        }
    }


    /* Вспомогательные функции класса */

    /**
     * Проверяет остались ли непроверенные лексемы
     * @return true or false
     */
    private fun checkLexemeIndex(): Boolean = lexemeIndex < LexemesController.lexemes.size

    /**
     * Проверяет является ли текущая лексема переданным ограничителем
     * @param delimiter ограничитель
     * @return true or false
     */
    private fun isDelimiter(delimiter: Delimiter): Boolean =
            checkLexemeIndex()
                    && currentLexeme.type == LexemeType.DELIMITER
                    && currentLexeme.toDelimiter() == delimiter

    /**
     * Проверяет является ли следующая лексема переданным ограничителем
     * @param delimiter ограничитель
     * @return true or false
     */
    private fun isNextDelimiter(delimiter: Delimiter): Boolean {
        ++lexemeIndex
        val result = checkLexemeIndex()
                && currentLexeme.type == LexemeType.DELIMITER
                && currentLexeme.toDelimiter() == delimiter
        --lexemeIndex
        return result
    }

    /**
     * Проверяет является ли текущая лексема переданным служебным словом
     * @param langWord служебное слово
     * @return true or false
     */
    private fun isLangWord(langWord: LangWord): Boolean = checkLexemeIndex()
            && currentLexeme.type == LexemeType.LANG_WORD
            && currentLexeme.toLangWord() == langWord

    /**
     * Пропустить лексемы до заданного служебного слова
     * @param langWord служебное слово
     */
    private fun skipTo(langWord: LangWord) {
        for (i in lexemeIndex until LexemesController.lexemes.size) {
            lexemeIndex = i - 1
            val lexeme = LexemesController.lexemes.elementAt(i)
            if (lexeme.type == LexemeType.LANG_WORD && lexeme.toLangWord() == langWord) break
        }
    }

    /**
     * Добавляет ошибку в список ошибок
     * @param lexemeIndex индекс лексемы в месте обнаружения ошибки
     * @param error текст ошибки
     */
    private fun addError(lexemeIndex: Int, error: String) {
        if (LexemesController.lexemes.isEmpty()) {
            errors.add("1 : 1 \t $error")
            return
        }

        val index = if (lexemeIndex < LexemesController.lexemes.size) lexemeIndex else LexemesController.lexemes.size - 1

        val lexemesLength = (0 until index)
                .map { LexemesController.lexemes.elementAt(it) }
                .sumBy {
                    when (it.type) {
                        LexemeType.NUMBER -> it.toNumber().value.length
                        LexemeType.DELIMITER -> it.toDelimiter().value.toString().length
                        LexemeType.LANG_WORD -> it.toLangWord().value.length
                        LexemeType.VAR_NAME -> it.toVarName().length
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

        errors.add("$textLine : $textLinePos \t '${LexemesController.lexemes.elementAt(index).value}' \t $error")
    }

    /**
     * Проверяет, является ли текущая лексема переданным служебным словом
     * Если да - добавляет ошибку в список ошибок и пропускает лексемы до заданного служебного слова
     * Возвращает результат добавления ошибки
     * @param langWord служебное слово для сравнения
     * @param skipTo служебное слово для пропуска
     * @return true or false
     */
    private fun addErrorIfNot(langWord: LangWord, skipTo: LangWord): Boolean {
        if (!checkLexemeIndex() || currentLexeme.type != LexemeType.LANG_WORD || currentLexeme.toLangWord() != langWord) {
            addError(lexemeIndex, "Ожидалось ключевое слово '${langWord.value}'")
            skipTo(skipTo)
            return true
        }
        return false
    }

    /**
     * Проверяет, является ли текущая лексема переданным ограничителем
     * Если да - добавляет ошибку в список ошибок и пропускает лексемы до заданного ограничителя
     * Возвращает результат добавления ошибки
     * @param delimiter ограничитель для сравнения
     * @param skipTo служебное слово для пропуска
     * @return true or false
     */
    private fun addErrorIfNot(delimiter: Delimiter, skipTo: LangWord): Boolean {
        if (!checkLexemeIndex() || currentLexeme.type != LexemeType.DELIMITER || currentLexeme.toDelimiter() != delimiter) {
            addError(lexemeIndex, "Ожидался символ '${delimiter.value}'")
            skipTo(skipTo)
            return true
        }
        return false
    }


    /* Расширения класса Lexeme */

    private fun Lexeme.toNumber(): Number = LexemesController.numbers.elementAt(index)
    private fun Lexeme.toDelimiter(): Delimiter = Delimiter.getAt(index)
    private fun Lexeme.toLangWord(): LangWord = LangWord.getAt(index)
    private fun Lexeme.toVarName(): String = LexemesController.variables.elementAt(index)

}
