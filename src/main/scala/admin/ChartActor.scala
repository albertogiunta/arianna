package admin

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

import common.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Interface
import ontologies.messages._

class ChartActor extends CustomActor {

    private var windowController: ChartWindowController = _
    private var cellInfo: InfoCell = _

    override def receive: Receive = {
        case msg@AriadneMessage(Interface, Interface.Subtype.OpenChart, _, cell: CellForChart) => {
            cellInfo = cell.info
            windowController initializeCharts cell.sensors
        }

        case msg@AriadneMessage(Interface, Interface.Subtype.UpdateChart, _, update: CellDataUpdate) => windowController updateCharts update.sensors

    }

    override def preStart(): Unit = {
        val loader = new FXMLLoader(getClass().getResource("chartWindowTemplate.fxml"));
        val template = loader.load
        windowController = loader.getController[ChartWindowController]
        val stage = new Stage
        stage.setOnCloseRequest((e) => {
            parent ! AriadneMessage(Interface, Interface.Subtype.CloseChart, Location.Admin >> Location.Self, cellInfo)
        })
        stage setTitle "Charts"
        stage setScene new Scene(template)
        stage.show
    }


}
