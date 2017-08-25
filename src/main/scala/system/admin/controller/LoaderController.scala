package system.admin.controller

import java.io.File
import javafx.fxml._
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{FileChooser, Stage}

import com.utils.InterfaceText
import run.LoaderListener

class LoaderController extends ViewController {

    var stage: Stage = _

    var listener: LoaderListener = _

    @FXML
    private def loadConfig(): Unit = {
        val fc = new FileChooser
        fc setTitle InterfaceText.FileSelectionText
        fc setSelectedExtensionFilter new ExtensionFilter(InterfaceText.Extension, "*.conf")
        val config: File = fc.showOpenDialog(null)
        listener onLoadConfig config.getAbsolutePath
        stage.close()
    }

}
