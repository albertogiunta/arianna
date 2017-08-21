package system.admin.controller

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.Initializable

/**
  * This trait is common to all controllers of the view. It extends Initializable, as requested
  * by JavaFX library and implements the empty method initialize.
  *
  **/
trait ViewController extends Initializable {

    override def initialize(location: URL, resources: ResourceBundle): Unit = {}

}
