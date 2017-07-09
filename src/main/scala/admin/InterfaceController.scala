package admin

import java.io.File
import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.text.Text

import akka.actor.ActorRef
import ontologies.messages._

import scala.io.Source
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

final case class CellForView(name: String, currentOccupation: Int, sensors: List[Sensor])

class InterfaceController extends Initializable {
    var actorRef: ActorRef = _
    var interfaceView: InterfaceView = _

    @FXML
    var nRooms: Text = _
    @FXML
    var fileName: Text = _
    @FXML
    var loadButton: Button = _

    var cellsBoxes: List[VBox] = _


    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        println("controller initialized")
        //loadButton.onAction = () => handleFileLoad()
    }

    def updateView(update: List[CellForView]): Unit = {
        Platform.runLater {
            //interfaceView.setText1("Niente")
            //interfaceView.setText2(update.currentPeople.toString)
        }
    }

    @FXML
    def handleFileLoad(): Unit = {
        val fc = new FileChooser()
        fc.setTitle("Get JSON")
        fc.getExtensionFilters.add(new ExtensionFilter("JSON Files", "*.json"))
        val json: File = fc.showOpenDialog(null)
        parseFile(json)
    }

    def parseFile(file: File): Unit = {
        val source = Source.fromFile(file).getLines.mkString
        actorRef ! AriadneLocalMessage(MessageType.Factory("Topology"), MessageSubtype.Factory("planimetrics"), Location.Admin >> Location.Self, source)
        fileName.text = file.getName
    }

    def createCells(initialConfiguration: List[CellForView]) = {
        nRooms.text = initialConfiguration.size.toString
    }


}
