package system.admin.controller

import java.io.File
import javafx.fxml._
import javafx.scene.control.TextField
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{FileChooser, Stage}

import com.utils.InterfaceText
import run.LoaderListener

class LoaderController extends ViewController {

    @FXML
    private var configField: TextField = _

    var stage: Stage = _

    var listener: LoaderListener = _

    @FXML
    private def loadConfig(): Unit = {
        val fc = new FileChooser
        fc setTitle InterfaceText.fileSelectionText
        fc setSelectedExtensionFilter new ExtensionFilter(InterfaceText.extension, "*.conf")
        val config: File = fc.showOpenDialog(null)
        listener onLoadConfig config.getAbsolutePath
        stage.close
    }

}
