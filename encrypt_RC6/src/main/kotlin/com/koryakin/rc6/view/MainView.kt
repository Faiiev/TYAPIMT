package com.koryakin.rc6.view

import com.koryakin.rc6.controller.MainController
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File

class MainView : View("Шифрование RC6") {

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

        input = textarea("This is the test input text for RC6 encryption/decryption program!") {
            vboxConstraints { vgrow = Priority.ALWAYS }
        }

        hbox(8.px.value, Pos.CENTER) {
            label("Ключ:")
            key = textfield("My test key") {
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
