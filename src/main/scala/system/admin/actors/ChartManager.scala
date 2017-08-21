package system.admin.actors

import javafx.application.Platform

import com.actors.CustomActor
import system.admin.controller.ChartWindowController
import system.admin.view.ChartView
import system.ontologies.messages.MessageType.Interface
import system.ontologies.messages._


/**
  * This actor handles the creation of a external window for charts, and it keeps them constantly updated with new
  * values received from the System (through his parent InterfaceManager).
  * It receives a CloseChart message when the window is closed.
  *
  **/
class ChartManager extends CustomActor {

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
            context.become(operational)
        }
    }

    def operational: Receive = {
        case msg@AriadneMessage(Interface, Interface.Subtype.UpdateChart, _, update: RoomDataUpdate) => {
            Platform.runLater(() => {
                windowController updateCharts update
            })
        }

        case msg@AriadneMessage(Interface, Interface.Subtype.CloseChart, _, _) => parent ! msg

    }


}
