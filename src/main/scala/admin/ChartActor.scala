package admin

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Interface
import ontologies.messages._

import scalafx.application.Platform

class ChartActor extends CustomActor {

    private var windowController: ChartWindowController = _
    private var cellInfo: InfoCell = _

    override def receive: Receive = {
        case msg@AriadneMessage(Interface, Interface.Subtype.OpenChart, _, cell: CellForChart) => {
            openWindow
            cellInfo = cell.info
            windowController initializeTitle cell.info.name
            windowController initializeCharts cell.sensorsId
        }

        case msg@AriadneMessage(Interface, Interface.Subtype.UpdateChart, _, update: CellForView) => windowController updateCharts update

    }

    def openWindow(): Unit = {
        val loader = new FXMLLoader(getClass().getResource("/chartWindowTemplate.fxml"));
        val template = loader.load[Pane]
        windowController = loader.getController[ChartWindowController]
        Platform.runLater {
            val stage = new Stage
            stage.setOnCloseRequest((e) => {
                parent ! AriadneMessage(Interface, Interface.Subtype.CloseChart, Location.Admin >> Location.Self, cellInfo)
            })
            stage setTitle "Arianna Charts"
            stage setScene new Scene(template)
            stage.show
        }
    }


}
