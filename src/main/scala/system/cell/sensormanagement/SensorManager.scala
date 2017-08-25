package system.cell.sensormanagement

import com.actors.{ClusterMembersListener, TemplateActor}
import spray.json._
import system.cell.sensormanagement.sensors._
import system.exceptions.IncorrectInitMessageException
import system.ontologies.messages
import system.ontologies.messages.AriannaJsonProtocol._
import system.ontologies.messages.MessageType.{Alarm, Update}
import system.ontologies.messages._

import scala.collection.mutable.{ListBuffer, _}

/**
  * This class implements an Actor that manage the different sensors of the cell,
  * It is responsive to the sensor update and checks if the sensors values exceeds
  * the threshold (set in the configuration file of the cell) in an reactive way
  * Created by Matteo Gabellini on 05/07/2017.
  */
class SensorManager extends TemplateActor {

    private var sensors = new HashMap[Int, SensorInfo]
    private val simulatedSensor: ListBuffer[SimulatedSensor[Double]] = new ListBuffer[SimulatedSensor[Double]]
    private val observableSensors: ListBuffer[ObservableSensor[_ <: Any]] = new ListBuffer[ObservableSensor[_ <: Any]]
    
    
    override protected def init(args: List[String]): Unit = {
        if (args.head == ClusterMembersListener.greetings) throw IncorrectInitMessageException(this.name, args)

        var sensorsToLoad = args.head.asInstanceOf[String].parseJson.convertTo[List[SensorInfoFromConfig]]
        sensorsToLoad foreach (X => {
            sensors += X.categoryId -> SensorInfo(X.categoryId, 0)
            val simSensor = SensorsFactory.createASensorFromConfig(X)
            simulatedSensor += simSensor.asInstanceOf[SimulatedSensor[Double]]
            observableSensors += SensorsFactory.createTheObservableVersion(simSensor)
        })
        initializeSensors()
        log.info(s"[{}] {} sensor/s initialized", this.name, sensorsToLoad.size)
    }

    private def initializeSensors(): Unit = {
        observableSensors foreach (X => {
            var flow = X.createObservable(SensorManager.observationRefreshMillis)
            flow.subscribe((Y: Double) => self ! SensorInfo(X.category.id, Y))
            X match {
                case x: SensorWithThreshold[Double] => {
                    flow.filter((K: Double) => x.threshold hasBeenExceeded K)
                        .subscribe((K: Double) => self ! AriadneMessage(
                            Alarm,
                            Alarm.Subtype.FromCell,
                            Location.PreMade.selfToSelf,
                            messages.SensorInfo(X.category.id, K)))
                }
            }
        })
    }


    override protected def receptive: Receive = {
        case msg: SensorInfo =>
            this.sensors += msg.categoryId -> msg
            this.parent ! AriadneMessage(
                Update,
                Update.Subtype.Sensors,
                Location.PreMade.selfToSelf,
                new SensorsInfoUpdate(CellInfo.empty, this.sensors.values.toList))
        case msg@AriadneMessage(Alarm, _, _, cnt: SensorInfo) =>
            this.parent ! msg
    }

    override def postStop(): Unit = {
        super.postStop()
        simulatedSensor foreach (X => X.stopGeneration())
        observableSensors foreach (X => X.stopObservation())
    }
}

object SensorManager {
    val observationRefreshMillis = 2000
}