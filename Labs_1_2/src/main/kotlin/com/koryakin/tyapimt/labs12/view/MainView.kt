package com.koryakin.tyapimt.labs12.view

import com.koryakin.tyapimt.labs12.app.MyApp
import tornadofx.*

class MainView : View("ТЯПиМТ. Лабораторные работы 1 и 2") {

    override val root = vbox(8.px.value) {
        minWidth = 800.px.value
        paddingAll = 8.px.value

        add(Lab1View::class)
        add(Lab2View::class)
    }

    init {
        shortcut("Ctrl+Q", { (app as MyApp).close() })
    }

}
