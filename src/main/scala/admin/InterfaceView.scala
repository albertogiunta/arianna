package admin

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.control.SplitPane
import javafx.stage.Stage

import scalafx.Includes._
import scalafx.scene.Scene

class InterfaceView extends Application {

    var controller: InterfaceController = _

    override def start(primaryStage: Stage): Unit = {
        primaryStage.setTitle("Admin Interface")
        primaryStage.setHeight(850)
        primaryStage.setWidth(1200)

        var loader: FXMLLoader = new FXMLLoader(getClass.getResource("/interface.fxml"))
        val root: SplitPane = loader.load()
        controller = loader.getController[InterfaceController]
        val scene: Scene = new Scene(root)

        primaryStage.setScene(scene)
        primaryStage.show()
    }

}
