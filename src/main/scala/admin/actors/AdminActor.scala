package admin.actors

import akka.actor.Props
import com.actors.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Topology}
import ontologies.messages._

/**
  * This actor intermediates between the interface and the System. It sends the loaded map, handles the messages coming
  * from the System and communicates them to the InterfaceActor in order to update the interface. It keeps a copy of the loaded map
  * if the System goes down and asks it again.
  *
  *
  */
class AdminActor() extends BasicActor {

    //Se si fa partire solo l'admin manager
    //private val adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/AdminManager")
    //Se si fa partire il master
    val adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/Master/AdminManager")
    private val interfaceActor = context.actorOf(Props[InterfaceActor])
    private var areaLoaded: Area = _
    private val toServer: MessageDirection = Location.Admin >> Location.Master
    private val toSelf: MessageDirection = Location.Admin >> Location.Self

    override def init(args: List[Any]): Unit = {
        interfaceActor ! AriadneMessage(Init, Init.Subtype.Greetings, toSelf, Greetings(List.empty))
    }

    override def receptive: Receive = {
        //Ricezione del messaggio iniziale dall'interfaccia con aggiornamento iniziale
        case msg@AriadneMessage(_, Topology.Subtype.Planimetrics, _, area: Area) => {
            areaLoaded = area
            adminManager ! msg.copy(direction = toServer)
            context.become(operational)
        }

        case _ => desist _

    }

    def operational: Receive = {

        case msg@AriadneMessage(_, MessageType.Update.Subtype.Admin, _, adminUpdate: AdminUpdate) => interfaceActor ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => adminManager ! msg.copy(direction = toServer)

        case msg@AriadneMessage(_, Alarm.Subtype.FromCell, _, content: AlarmContent) => interfaceActor ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, sensorsInfo: SensorsInfoUpdate) => interfaceActor ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Error, Error.Subtype.LookingForAMap, _, _) => adminManager ! AriadneMessage(Topology, Topology.Subtype.Planimetrics, toServer, areaLoaded)

        case msg@AriadneMessage(Error, Error.Subtype.MapIdentifierMismatch, _, _) => interfaceActor ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => adminManager ! msg.copy(direction = toServer)

        case _ => desist _

    }

}
