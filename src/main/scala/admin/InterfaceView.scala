package admin

import java.io.File
import javafx.application.Application
import javafx.stage.Stage

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{GridPane, Priority, VBox}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

class InterfaceView extends Application {

    var label: Label = new Label("Temperature")
    var label1: Label = new Label()
    var label2: Label = new Label("People")
    var label3: Label = new Label()
    var controller: InterfaceController = _

    override def start(primaryStage: Stage): Unit = {
        primaryStage.setTitle("Admin Interface")
        primaryStage.setHeight(200)
        primaryStage.setWidth(250)
        val scene: Scene = new Scene {
            root = {
                label.alignmentInParent = Pos.BaselineRight

                GridPane.setConstraints(label, 0, 0, 1, 1)

                label1.alignmentInParent = Pos.BaselineLeft

                GridPane.setConstraints(label1, 1, 0, 2, 1)

                label2.alignmentInParent = Pos.BaselineRight

                GridPane.setConstraints(label2, 0, 1, 1, 1)

                label3.alignmentInParent = Pos.BaselineLeft

                GridPane.setConstraints(label3, 1, 1, 5, 1)

                var chooseBtn: Button = new Button("Choose")
                chooseBtn.alignmentInParent = Pos.BaselineCenter
                chooseBtn.onAction = handle {
                    handleFileLoad()
                }
                GridPane.setConstraints(chooseBtn, 0, 2)

                var alarmBtn: Button = new Button("Alarm!")
                alarmBtn.alignmentInParent = Pos.BaselineCenter
                alarmBtn.onAction = handle {
                    handleAlarm()
                }
                GridPane.setConstraints(alarmBtn, 1, 2)

                val grid1 = new GridPane {
                    hgap = 4
                    vgap = 6
                    margin = Insets(18)
                    children = Seq(label, label1, label2, label3, chooseBtn, alarmBtn)
                }
                new VBox {
                    vgrow = Priority.Always
                    hgrow = Priority.Always
                    spacing = 10
                    padding = Insets(20)
                    children = List(
                        new VBox {
                            children = List(new Label("Maps data:"), grid1)
                        }
                    )
                }
            }
        }

        primaryStage.setScene(scene)
        primaryStage.show()
    }

    def handleFileLoad(): Unit = {
        val fc = new FileChooser()
        fc.setTitle("Get JSON")
        fc.getExtensionFilters.add(new ExtensionFilter("JSON Files", "*.json"))
        val json: File = fc.showOpenDialog(null)
        controller.parseFile(json)
    }

    def handleAlarm(): Unit = {
        controller.triggerAlarm
    }
    def setText1(s: String): Unit = {
        label1.setText(s)
    }

    def setText2(s: String): Unit = {
        label3.setText(s)
    }


}
