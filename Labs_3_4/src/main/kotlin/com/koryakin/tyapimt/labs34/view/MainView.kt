package com.koryakin.tyapimt.labs34.view

import com.koryakin.tyapimt.labs34.app.MyApp
import com.koryakin.tyapimt.labs34.controller.LabController
import com.koryakin.tyapimt.labs34.controller.LexemesController
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import tornadofx.*

class MainView : View("ТЯПиМТ. Лабораторные работы 3 и 4") {

    private companion object {
        val OPEN_FILE_FILTERS = arrayOf(
                FileChooser.ExtensionFilter("Текстовые файлы", "*.txt")
        )
    }

    private val controller: LabController by inject()
    private var input: TextArea by singleAssign()
    private var output: TextArea by singleAssign()

    override val root = vbox(8.px.value) {
        minWidth = 800.px.value
        paddingAll = 8.px.value

        hbox(8.px.value, Pos.CENTER_LEFT) {
            label("Текст программы:")
            val openFileBtn = button("Загрузить из файла")
            val fileLabel = label()
            openFileBtn.action {
                val files = chooseFile("Открыть файл", OPEN_FILE_FILTERS, FileChooserMode.Single, currentWindow)
                if (files.isNotEmpty()) {
                    val file = files.first()
                    input.text = file.readText()
                    fileLabel.text = file.path
                }
            }
        }

        input = textarea {
            vboxConstraints { vgrow = Priority.ALWAYS }
            isFocusTraversable = false

            textProperty().onChange {
                controller.start(text)
            }
        }

        label("Обнаруженные ошибки:")

        output = textarea {
            isEditable = false
        }
    }

    init {
        shortcut("Ctrl+Q", { (app as MyApp).close() })

        controller.errors.onChange { change ->
            output.clear()
            change.list.forEach {
                output.appendText(it + "\n")
            }
        }
    }

}
