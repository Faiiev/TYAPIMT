package com.koryakin.tyapimt.labs12.app

import com.koryakin.tyapimt.labs12.view.MainView
import javafx.stage.Stage
import javafx.stage.WindowEvent
import tornadofx.*

class MyApp : App(MainView::class) {

    private lateinit var primaryStage: Stage

    override fun start(stage: Stage) {
        super.start(stage)

        stage.setOnCloseRequest { event ->
            event.consume()
            AppExitFragment().openModal()
        }

        primaryStage = stage
    }

    fun close() {
        val closeEvent = WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)
        primaryStage.fireEvent(closeEvent)
    }

}
