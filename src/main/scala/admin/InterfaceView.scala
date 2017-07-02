package admin

import javafx.application.Application
import javafx.stage.Stage

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.{GridPane, Priority, VBox}

class InterfaceView extends Application {

    var label: Label = new Label("Temperature")
    var label1: Label = new Label()
    var label2: Label = new Label("People")
    var label3: Label = new Label()

    override def start(primaryStage: Stage): Unit = {
        primaryStage.setTitle("Admin Interface")
        primaryStage.setHeight(150)
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

                val grid1 = new GridPane {
                    hgap = 4
                    vgap = 6
                    margin = Insets(18)
                    children = Seq(label, label1, label2, label3)
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

    def setText1(s: String): Unit = {
        label1.setText(s)
    }

    def setText2(s: String): Unit = {
        label3.setText(s)
    }

}
