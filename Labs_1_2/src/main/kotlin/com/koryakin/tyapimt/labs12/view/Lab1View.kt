package com.koryakin.tyapimt.labs12.view

import com.koryakin.tyapimt.labs12.controller.Lab1Controller
import com.koryakin.tyapimt.labs12.controller.LexemesController
import com.koryakin.tyapimt.labs12.model.Lex
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.*

class Lab1View : View() {

    private val controller: Lab1Controller by inject()
    private var inputField: TextField by singleAssign()

    override val root = vbox(8.px.value) {
        vboxConstraints { vgrow = Priority.ALWAYS }

        hbox(8.px.value, Pos.CENTER) {
            label("Введите строку:")

            inputField = textfield {
                hboxConstraints { hgrow = Priority.ALWAYS }

                // предотвращаем выделение текста, перемещение указателя и стирание данных
                selectionProperty().addListener { _, _, _ -> deselect() }
                caretPositionProperty().addListener { _, _, _ -> positionCaret(text.length) }
                setOnKeyPressed { if (it.code == KeyCode.BACK_SPACE) it.consume() }

                // вызываем контроллер при добавлении нового символа
                textProperty().addListener { _, _, newValue ->
                    when {
                        newValue.isEmpty() -> return@addListener
                        controller.incorrectChar != null -> controller.incorrectChar = null
                        else -> controller.nextChar(newValue.last())
                    }
                }

                // отключаем элемент в конце строки
                disableWhen(LexemesController.isLineEndedProperty)
            }
        }

        button("Сбросить").action {
            controller.reset()
            inputField.clear()
            inputField.requestFocus()
        }

        separator()

        label("Лексемы:")

        tableview(LexemesController.lexemes) {
            vboxConstraints { vgrow = Priority.ALWAYS }
            columnResizePolicy = SmartResize.POLICY

            column("ID", Lex::id)
            column("Лексема", Lex::value).remainingWidth()
            column("Тип лексемы", Lex::type).remainingWidth()
            column("Индекс в таблице", Lex::index).remainingWidth()
        }
    }

    init {
        controller.incorrectCharProperty.onChange {
            it?.let { IncorrectCharFragment().openModal() }
        }
    }

    inner class IncorrectCharFragment : Fragment("Ошибка!") {

        override val root = form {
            minWidth = 250.px.value

            fieldset {
                field { label("Недопустимый символ '${controller.incorrectChar}'") }
                buttonbar {
                    button("ОК").action {
                        inputField.deletePreviousChar()
                        close()
                    }
                }
            }
        }

    }

}
