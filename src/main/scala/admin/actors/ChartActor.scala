package admin.actors

import javafx.application.Platform

import admin.controller.ChartWindowController
import admin.view.ChartView
import com.actors.CustomActor
import ontologies.messages.MessageType.Interface
import ontologies.messages._


/**
  * This actor handles the creation of a external window for charts, and it keeps them constantly updated with new
  * values received from the System (through his parent AdminActor).
  *
  *
  **/
class ChartActor extends CustomActor {

    private var windowController: ChartWindowController = _

    override def receive: Receive = {
        case msg@AriadneMessage(Interface, Interface.Subtype.OpenChart, _, cell: CellForChart) => {
            Platform.runLater(() => {
                val view = new ChartView
                view.start
                windowController = view.controller
                windowController.chartActor = self
                windowController initializeWindow cell.cell
                windowController initializeCharts cell.sensorsId
            })

        }

        case msg@AriadneMessage(Interface, Interface.Subtype.UpdateChart, _, update: RoomDataUpdate) => {
            Platform.runLater(() => {
                windowController updateCharts update
            })
        }

        case msg@AriadneMessage(Interface, Interface.Subtype.CloseChart, _, _) => parent forward msg

    }


}
