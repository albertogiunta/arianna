package system.admin.view

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.stage.Stage

import com.utils.{GraphicResources, InterfaceText}
import system.admin.controller.InterfaceController


/**
  * This class represent the interface of the Application
  *
  **/
class InterfaceView extends View {

    var controller: InterfaceController = _

    /**
      * Main method that starts the View and show it to the user
      **/
    def start(): Unit = {
        val mainStage = new Stage
        mainStage setTitle InterfaceText.MainTitle
        mainStage setHeight 850
        mainStage setWidth 1200
        mainStage setResizable false
        val loader: FXMLLoader = new FXMLLoader(getClass.getResource(GraphicResources.Interface))
        val root: SplitPane = loader.load[SplitPane]
        controller = loader.getController[InterfaceController]
        val scene: Scene = new Scene(root)
        mainStage.setOnCloseRequest((e) => {
            Platform.exit()
            System.exit(0)
        })
        mainStage setScene scene
        mainStage.show()
    }

}
