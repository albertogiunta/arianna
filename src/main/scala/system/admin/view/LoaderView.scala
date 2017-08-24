package system.admin.view

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

import com.utils.{GraphicResources, InterfaceText}
import system.admin.controller.LoaderController

class LoaderView extends View {

    var controller: LoaderController = _

    /**
      * Main method to start this View
      **/
    override def start(): Unit = {
        val mainStage = new Stage
        mainStage setTitle InterfaceText.loadTitle
        mainStage setHeight 125
        mainStage setWidth 383
        mainStage setResizable false
        var loader: FXMLLoader = new FXMLLoader(getClass.getResource(GraphicResources.loader))
        val root: Pane = loader.load[Pane]
        controller = loader.getController[LoaderController]
        controller.stage = mainStage
        val scene: Scene = new Scene(root)
        mainStage.setOnCloseRequest((e) => {
            Platform.exit
            System.exit(0)
        })
        mainStage setScene scene
        mainStage.getScene.getWindow setHeight 125
        mainStage.getScene.getWindow setWidth 383
        mainStage.show
    }
}
