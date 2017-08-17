package admin.view

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

import admin.controller.{ChartWindowController, GraphicResources}

/**
  * This class represent the View for the secondary window containing charts.
  **/
class ChartView() extends View {

    var controller: ChartWindowController = _

    def start(): Unit = {
        val loader = new FXMLLoader(getClass.getResource(GraphicResources.chartWindow));
        val template = loader.load[Pane]
        controller = loader.getController[ChartWindowController]
        val stage = new Stage
        stage.setOnCloseRequest((e) => {
            controller.closeWindow
        })
        stage setTitle "Arianna Charts"
        stage setScene new Scene(template)
        stage setResizable false
        stage.show
    }

}
