package admin

import area.SampleUpdate
import scalafx.application.Platform
/**
  * Created by lisamazzini on 30/06/17.
  */
class InterfaceController(interfaceView: InterfaceView)  {
    def newText(sampleUpdate: SampleUpdate): Unit ={
        Platform.runLater {
            interfaceView.setText1(sampleUpdate.temperature.toString)
            interfaceView.setText2(sampleUpdate.people.toString)
        }
    }

}
