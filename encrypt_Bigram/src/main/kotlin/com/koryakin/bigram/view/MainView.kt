package com.koryakin.bigram.view

import com.koryakin.bigram.controller.MainController
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File

class MainView : View("Шифрование биграммами") {

    private val controller: MainController by inject()
    private var input: TextArea by singleAssign()
    private var key: TextField by singleAssign()
    private var output: TextArea by singleAssign()
    private var saveFile: File? = null

    override val root = vbox(8.px.value) {
        minWidth = 800.px.value
        paddingAll = 8.px.value

        hbox(8.px.value, Pos.CENTER_LEFT) {
            label("Исходный текст:")
            val openFileBtn = button("Из файла")
            val fileLabel = label()
            openFileBtn.action {
                val filters = arrayOf(FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"))
                val files = chooseFile("Открыть файл", filters, FileChooserMode.Single, currentWindow)
                if (files.isNotEmpty()) {
                    val file = files.first()
                    input.text = file.readText()
                    fileLabel.text = file.path
                } else {
                    input.text = ""
                    fileLabel.text = ""
                }
            }
        }

        input = textarea {
            vboxConstraints { vgrow = Priority.ALWAYS }

            textProperty().addListener { _, _, newValue ->
                text = newValue.filter { MainController.SYMBOLS.contains(it) }
            }
        }

        hbox(8.px.value, Pos.CENTER) {
            label("Ключ:")
            key = textfield {
                hboxConstraints { hgrow = Priority.ALWAYS }
            }
            button("Зашифровать").action {
                output.text = controller.encrypt(input.text, key.text)
                if (saveFile != null) {
                    saveFile?.writeText(output.text)
                }
            }
            button("Дешифровать").action {
                output.text = controller.decrypt(input.text, key.text)
                if (saveFile != null) {
                    saveFile?.writeText(output.text)
                }
            }
        }

        hbox(8.px.value, Pos.CENTER_LEFT) {
            label("Зашифрованный/дешифрованный текст:")
            val openFileBtn = button("Сохранять в файл")
            val fileLabel = label()
            openFileBtn.action {
                val filters = arrayOf(FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"))
                val files = chooseFile("Открыть файл", filters, FileChooserMode.Single, currentWindow)
                if (files.isNotEmpty()) {
                    saveFile = files.first()
                    fileLabel.text = saveFile?.path
                } else {
                    saveFile = null
                    fileLabel.text = ""
                }
            }
        }

        output = textarea {
            vboxConstraints { vgrow = Priority.ALWAYS }
            isEditable = false
        }
    }

}
