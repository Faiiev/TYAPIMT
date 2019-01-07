package com.koryakin.tyapimt.labs12.app

import javafx.application.Platform
import tornadofx.*

class AppExitFragment : Fragment("Выйти из программы?") {

    override val root = form {
        minWidth = 300.px.value

        fieldset {
            field { label("Выйти из программы?") }
            buttonbar {
                button("Да").action { Platform.exit() }
                button("Нет").action { close() }
            }
        }
    }

}
