package cell.sensormanagement

import cell.sensormanagement.sensors._
import common.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Update}
import ontologies.messages.{AriadneMessage, Location, MessageDirection}

import scala.collection.mutable.ListBuffer

/**
  * This class implements an Actor that manage the different sensors of the cell,
  * It is responsive to the sensor update and checks if the sensors values exceeds
  * the threshold (set in the configuration file of the cell) in an reactive way
  * Created by Matteo Gabellini on 05/07/2017.
  */
class SensorManagerActor extends BasicActor {
    private val observationRefresh = 1000

    //private var sensors: Map[String, Sensor] = new mutable.HashMap[String, Sensor]
    private val observableSensors: ListBuffer[ObservableSensor[Any]] = new ListBuffer[ObservableSensor[Any]]

    private val internalMessage: MessageDirection = Location.Self >> Location.Self


    override protected def init(args: List[Any]): Unit = {
        //Load the sensor list to initialize from the config file
        //        var tSensor = new BasicTemperatureSensor("tSensor1", 0, -40.0, 100.0, 100.0 - -40.0)
        //        var simulatedTempSensor = new SimulatedMonotonicTemperatureSensor(tSensor, 1000, 0.15)
        //        var oTSensor: ObservableTemperatureSensor = new ObservableTemperatureSensor(simulatedTempSensor)
        //        oTSensor.createObservable(1000)
        //            .subscribe((X:Double)=> self ! ontologies.messages.Sensor(oTSensor.category.id, X.asInstanceOf[Double]))
        //
        //        var gasSensor = new BasicGasSensor("CO2Sensor", 0, 0, 50.0, 50.0, "CO2")
        //        var simulatedGasSensor = new SimulatedMonotonicGasSensor(gasSensor, 1000, 0.2)
        //        var oGSensor = new ObservableGasSensor(simulatedGasSensor)
        //
        //        oGSensor.createObservable(1000)
        //            .subscribe(X => self ! ontologies.messages.Sensor(oGSensor.category, X.asInstanceOf[Double]))
    }

    override protected def receptive: Receive = {
        case msg: ontologies.messages.Sensor =>
            this.parent ! AriadneMessage(Update,
                Update.Subtype.Sensors,
                internalMessage, msg)
        case msg@AriadneMessage(Alarm, _, _, _) =>
            this.parent ! msg
    }

    private def loadSensorfromConfig(): Unit = {

    }

    private def initializeSensors(): Unit = {
        observableSensors foreach (X => {
            var flow = X.createObservable(observationRefresh)
            flow.subscribe(Y => self ! ontologies.messages.Sensor(X.category.id, Y))
            if (X.isInstanceOf[SensorWithThreshold[_ <: AnyVal]]) {
                flow.filter(K => X.asInstanceOf[SensorWithThreshold[_ <: Any]].threshold hasBeenExceeded K)
                    .subscribe(K => self ! AriadneMessage(
                        Alarm,
                        Alarm.Subtype.Basic,
                        internalMessage,
                        ontologies.messages.Sensor(X.category.id, K.asInstanceOf[Double])))
            }
        })
    }

    override def postStop(): Unit = {
        super.postStop()
        observableSensors foreach (X => X.stopObservation())
    }
}
