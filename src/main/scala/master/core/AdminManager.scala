package master.core

import akka.actor.ActorSelection
import com.actors.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Update}
import ontologies.messages._

import scala.collection.mutable.ListBuffer

/**
  * This is the Actor inside the cluster that forwards messages to the Administrator system.
  *
  **/
class AdminManager extends CustomActor {
    
    val toAdmin: MessageDirection = Location.Master >> Location.Admin
    val fromAdmin: MessageDirection = Location.Admin >> Location.Master
    val admin = context.actorSelection("akka.tcp://adminSystem@127.0.0.1:4550/user/admin")
    val topologySupervisor: ActorSelection = sibling("TopologySupervisor").get
    val publisher: ActorSelection = sibling("Publisher").get

    def operational: Receive = {
        //Ricezione di un update dal server
        case msg@AriadneMessage(Update, Update.Subtype.Admin, _, content: AdminUpdate) => admin ! roundData(content)

        //Ricezione di un allarme dall'admin
        case msg@AriadneMessage(Alarm, Alarm.Subtype.FromInterface, fromAdmin, _) => publisher ! msg.copy(direction = fromAdmin)
        //Ricezione di un allarme da parte del sistema
        case msg@AriadneMessage(Alarm, Alarm.Subtype.FromCell, _, _) => admin ! msg.copy(direction = toAdmin)
        //Ricezione di aggiornamento sensori
        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, _) => admin ! msg

        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => parent ! msg.copy(direction = fromAdmin)

        case msg@AriadneMessage(Error, Error.Subtype.MapIdentifierMismatch, _, _) => admin ! msg.copy(direction = toAdmin)

        case msg@AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, _, area: Area) => topologySupervisor ! msg

    }

    override def receive: Receive = {
        case msg@AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, _, area: Area) => {
            topologySupervisor ! msg
            log.info("Map received from Admin")
            context.become(operational)
        }
        case msg@AriadneMessage(Error, Error.Subtype.LookingForAMap, _, _) => admin ! msg

    }

    private def roundData(adminUpdate: AdminUpdate): AriadneMessage[AdminUpdate] = {
        val roundedRoomDataUpdate: ListBuffer[RoomDataUpdate] = new ListBuffer[RoomDataUpdate]
        adminUpdate.list.foreach(roomDataUpdate => {
            val roundedSensorData: ListBuffer[SensorInfo] = new ListBuffer[SensorInfo]
            roomDataUpdate.cell.sensors.foreach(sensor => roundedSensorData += SensorInfo(sensor.categoryId, round(sensor.value)))
            roundedRoomDataUpdate += roomDataUpdate.copy(cell = ontologies.messages.Cell(roomDataUpdate.cell.info, roundedSensorData.toList))
        })

        AriadneMessage(Update, Update.Subtype.Admin, Location.Master >> Location.Admin, adminUpdate.copy(list = roundedRoomDataUpdate.toList))

    }

    private def round(data: Double): Double = {
        BigDecimal(data).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
}
