package cell.sensormanagement

import common.BasicActor

/**
  * Created by Matteo Gabellini on 05/07/2017.
  */
class SensorManagerActor extends BasicActor {
    override protected def init(args: List[Any]): Unit = {
        //Load the sensor list to initialize from the config file
    }

    override protected def receptive: Receive = ???
}
