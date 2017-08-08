package admin.view

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.stage.Stage

import admin.controller.InterfaceController


/**
  * This class represent the interface of the Application
  *
  **/
class InterfaceView {

    var controller: InterfaceController = _

    def start(): Unit = {
        val mainStage = new Stage
        mainStage setTitle "Arianna admin Interface"
        mainStage setHeight 850
        mainStage setWidth 1200
        mainStage setResizable false
        var loader: FXMLLoader = new FXMLLoader(getClass.getResource("/interface.fxml"))
        val root: SplitPane = loader.load[SplitPane]
        controller = loader.getController[InterfaceController]
        val scene: Scene = new Scene(root)
        mainStage.setOnCloseRequest((e) => {
            Platform.exit
            System.exit(0)
        })
        mainStage setScene scene
        mainStage.show
    }

}
