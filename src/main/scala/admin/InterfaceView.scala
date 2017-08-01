package admin

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.control.SplitPane
import javafx.stage.Stage

import scalafx.Includes._
import scalafx.scene.Scene

/**
  * This class represent the interface of the Application
  *
  **/
class InterfaceView {

    var controller: InterfaceController = _

    def start(): Unit = {
        val mainStage = new Stage
        mainStage.title = "Arianna admin Interface"
        mainStage.height = 850
        mainStage.width = 1200
        mainStage.resizable = false
        var loader: FXMLLoader = new FXMLLoader(getClass.getResource("/interface.fxml"))
        val root: SplitPane = loader.load()
        controller = loader.getController[InterfaceController]
        val scene: Scene = new Scene(root)
        mainStage.setOnCloseRequest(() => {
            Platform.exit()
            System.exit(0)
        })
        mainStage setScene scene
        mainStage.show()
    }

}
