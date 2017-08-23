package system.master.core

import akka.actor.ActorSelection
import com.actors.CustomActor
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Topology, Update}
import system.ontologies.messages._

import scala.collection.mutable.ListBuffer

/**
  * This is the Actor inside the cluster that forwards messages to the Administrator system from the Cluster,
  * and viceversa. It also modifies data from Cluster, in order to make it ready to be shown on the Interface.
  *
  **/
class AdminSupervisor extends CustomActor {
    
    private val IPAddress: String = configManager.config("Ariadne-Admin")
        .getString("akka.remote.netty.tcp.hostname")
    
    private val port: Int = configManager.config("Ariadne-Admin")
        .getNumber("akka.remote.netty.tcp.port").intValue()
    
    private val toAdmin: MessageDirection = Location.Master >> Location.Admin
    private val fromAdmin: MessageDirection = Location.Admin >> Location.Master
    println("akka.tcp://" + NamingSystem.AdminActorSystem + "@" + IPAddress + ":" + port + "/user/" + NamingSystem.AdminManager)
    private val admin = context.actorSelection("akka.tcp://" + NamingSystem.AdminActorSystem + "@" + IPAddress + ":" + port + "/user/" + NamingSystem.AdminManager)
    private val topologySupervisor: ActorSelection = sibling(NamingSystem.TopologySupervisor).get
    private val publisher: ActorSelection = sibling(NamingSystem.Publisher).get

    def operational: Receive = {
        case msg@AriadneMessage(Topology, Topology.Subtype.Acknowledgement, _, _) => admin ! msg

        case msg@AriadneMessage(Update, Update.Subtype.Admin, _, content: AdminUpdate) => admin ! roundData(content)

        case msg@AriadneMessage(Alarm, Alarm.Subtype.FromInterface, fromAdmin, _) => publisher ! msg.copy(direction = fromAdmin)

        case msg@AriadneMessage(Alarm, Alarm.Subtype.FromCell, _, _) => admin ! msg.copy(direction = toAdmin)

        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) => publisher ! msg.copy(direction = toAdmin)

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
            roundedRoomDataUpdate += roomDataUpdate.copy(cell = system.ontologies.messages.Cell(roomDataUpdate.cell.info, roundedSensorData.toList))
        })

        AriadneMessage(Update, Update.Subtype.Admin, Location.Master >> Location.Admin, adminUpdate.copy(list = roundedRoomDataUpdate.toList))

    }

    private def round(data: Double): Double = {
        BigDecimal(data).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
}
