package admin

import area.SampleUpdate

import scalafx.application.Platform

class InterfaceController(interfaceView: InterfaceView) {
    def newText(sampleUpdate: SampleUpdate): Unit = {
        Platform.runLater {
            interfaceView.setText1(sampleUpdate.temperature.toString)
            interfaceView.setText2(sampleUpdate.people.toString)
        }
    }
}
