package cell.sensormanagement

import cell.sensormanagement.sensors._
import com.actors.BasicActor
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Update}
import ontologies.messages._
import spray.json._

import scala.collection.mutable.{ListBuffer, _}

/**
  * This class implements an Actor that manage the different sensors of the cell,
  * It is responsive to the sensor update and checks if the sensors values exceeds
  * the threshold (set in the configuration file of the cell) in an reactive way
  * Created by Matteo Gabellini on 05/07/2017.
  */
class SensorManager extends BasicActor {
    private val observationRefresh = 1000

    private var sensors = new HashMap[Int, SensorInfo]
    private val simulatedSensor: ListBuffer[SimulatedSensor[Double]] = new ListBuffer[SimulatedSensor[Double]]
    private val observableSensors: ListBuffer[ObservableSensor[_ <: Any]] = new ListBuffer[ObservableSensor[_ <: Any]]

    private val internalMessage: MessageDirection = Location.Self >> Location.Self


    override protected def init(args: List[Any]): Unit = {

        var sensorsToLoad = args(0).asInstanceOf[String].parseJson.convertTo[List[SensorInfoFromConfig]]
        sensorsToLoad foreach (X => {
            sensors.put(X.categoryId, SensorInfo(X.categoryId, 0))
            val simSensor = SensorsFactory.createASensorFromConfig(X)
            simulatedSensor += simSensor.asInstanceOf[SimulatedSensor[Double]]
            observableSensors += SensorsFactory.createTheObservableVersion(simSensor)
        })
        initializeSensors()

    }

    private def initializeSensors(): Unit = {
        observableSensors foreach (X => {
            var flow = X.createObservable(observationRefresh)
            flow.subscribe((Y: Double) => self ! ontologies.messages.SensorInfo(X.category.id, Y))
            X match {
                case x: SensorWithThreshold[Double] => {
                    flow.filter((K: Double) => x.threshold hasBeenExceeded K)
                        .subscribe((K: Double) => self ! AriadneMessage(
                            Alarm,
                            Alarm.Subtype.Basic,
                            internalMessage,
                            ontologies.messages.SensorInfo(X.category.id, K)))
                }
            }
        })
    }


    override protected def receptive: Receive = {
        case msg: ontologies.messages.SensorInfo =>
            this.sensors.put(msg.categoryId, msg)
            this.parent ! AriadneMessage(Update,
                Update.Subtype.Sensors,
                internalMessage, new SensorsInfoUpdate(InfoCell.empty, this.sensors.values.toList))
        case msg@AriadneMessage(Alarm, _, _, cnt: SensorInfo) =>
            this.parent ! msg
    }

    override def postStop(): Unit = {
        super.postStop()
        simulatedSensor foreach (X => X.stopGeneration())
        observableSensors foreach (X => X.stopObservation())
    }
}
