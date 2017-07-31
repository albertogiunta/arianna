package admin

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

/**
  * This class represent the View for the secondary window containing charts.
  **/
class ChartView() {

    var controller: ChartWindowController = _

    def start(): Unit = {
        val loader = new FXMLLoader(getClass().getResource("/chartWindowTemplate.fxml"));
        val template = loader.load[Pane]
        controller = loader.getController[ChartWindowController]
        val stage = new Stage
        stage.setOnCloseRequest((e) => {
            controller.closeView()
        })
        stage setTitle "Arianna Charts"
        stage setScene new Scene(template)
        stage.show
    }

}
