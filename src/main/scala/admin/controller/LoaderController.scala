package admin.controller

import javafx.fxml._
import javafx.scene.control.TextField
import javafx.stage.Stage

import run.LoaderListener

class LoaderController extends ViewController {

    @FXML
    private var configField: TextField = _

    var stage: Stage = _

    var listener: LoaderListener = _

    @FXML
    private def loadConfig(): Unit = {
        listener onLoadConfig configField.getText
        stage.close
    }

}
