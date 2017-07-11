package admin

import java.io.File
import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.{GridPane, Pane, VBox}
import javafx.scene.text.Text

import akka.actor.ActorRef
import ontologies.messages._

import scala.io.Source
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

final case class CellForView(id: Int, name: String, currentOccupation: Int, sensors: List[Sensor])

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

    @FXML
    var gridPane: GridPane = _


    override def initialize(location: URL, resources: ResourceBundle): Unit = {
        println("controller initialized")
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
        var x = 0
        var y = 0
        initialConfiguration.foreach(c => {
            var node = createCellTemplate(c)

            //println("prova " + prova.toString())
            Platform.runLater {
                gridPane.add(node, x, y)
                if (y == 1) {
                    y = 0
                    x = x + 1
                } else {
                    y = y + 1
                }
            }


        })
    }

    def createCellTemplate(c: CellForView): Node = {
        var node = new FXMLLoader(getClass.getResource("/cellTemplate.fxml")).load[VBox]
        node.setId(c.id.toString)

        var nameText = getTextNode(node, "namePane", "roomName")
        nameText.text = c.name

        var occupationText = getTextNode(node, "occupationPane", "roomOccupation")
        occupationText.text = c.currentOccupation.toString

        var temperatureText = getTextNode(node, "temperaturePane", "roomTemperature")
        temperatureText.text = c.sensors.filter(s => s.category.equals(1)).head.value.toString
        node
    }

    def getTextNode(node: VBox, paneId: String, textId: String): Text = {
        var pane = node.getChildren.filter(c => c.id.value.equals(paneId)).get(0).asInstanceOf[Pane]
        var text = pane.getChildren.filter(c => c.id.value.equals(textId)).get(0).asInstanceOf[Text]
        text
    }


}
