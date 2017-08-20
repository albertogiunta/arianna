package master.core

import akka.actor.ActorSelection
import com.actors.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Update}
import ontologies.messages._
import system.names.NamingSystem

import scala.collection.mutable.ListBuffer

/**
  * This is the Actor inside the cluster that forwards messages to the Administrator system.
  *
  **/
class AdminSupervisor extends CustomActor {

    private val IPAddress: String = "127.0.0.1"
    private val port: String = "4550"
    private val toAdmin: MessageDirection = Location.Master >> Location.Admin
    private val fromAdmin: MessageDirection = Location.Admin >> Location.Master
    private val admin = context.actorSelection("akka.tcp://" + NamingSystem.AdminActorSystem + "@" + IPAddress + ":" + port + "/user/" + NamingSystem.AdminManager)
    private val topologySupervisor: ActorSelection = sibling(NamingSystem.TopologySupervisor).get
    private val publisher: ActorSelection = sibling(NamingSystem.Publisher).get

    def operational: Receive = {
        //Ricezione di un update dal server
        case msg@AriadneMessage(Update, Update.Subtype.Admin, _, content: AdminUpdate) => admin ! roundData(content)

        //Ricezione di un allarme dall'admin
        case msg@AriadneMessage(Alarm, Alarm.Subtype.FromInterface, fromAdmin, _) => publisher ! msg.copy(direction = fromAdmin)
        //Ricezione di un allarme da parte del sistema
        case msg@AriadneMessage(Alarm, Alarm.Subtype.FromCell, _, _) => admin ! msg.copy(direction = toAdmin)

        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) => publisher ! msg.copy(direction = toAdmin)
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
    
    private def roundData(adminUpdate: AdminUpdate): AriadneMessage[MessageContent] = {
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
