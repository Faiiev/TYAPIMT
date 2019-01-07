package com.koryakin.tyapimt.labs12.controller.lab2

import com.koryakin.tyapimt.labs12.controller.LexemesController
import com.koryakin.tyapimt.labs12.model.Lex
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import tornadofx.*

abstract class BaseClass(val controller: Lab2Controller) {

    val numberProperty = SimpleDoubleProperty(0.0)
    var number by numberProperty
        protected set

    var reset = false
        protected set

    val isReadyProperty = SimpleBooleanProperty(false)
    protected var isReady by isReadyProperty

    init {
        controller.resetFlagProperty.onChange { reset = true }
        LexemesController.isLineEndedProperty.onChange { isReady = true }
    }

    abstract fun start(lex: Lex?)

}
