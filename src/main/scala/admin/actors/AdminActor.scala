package admin.actors

import akka.actor.Props
import com.actors.BasicActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Topology}
import ontologies.messages._
import system.names.NamingSystem

/**
  * This actor intermediates between the interface and the System. It sends the loaded map, handles the messages coming
  * from the System and communicates them to the InterfaceManager in order to update the interface. It keeps a copy of the loaded map
  * if the System goes down and asks it again.
  *
  *
  */
class AdminActor extends BasicActor {

    //Se si fa partire solo l'admin manager
    //private val adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/AdminManager")
    //Se si fa partire il master
    private var adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/Master/AdminManager")
    private val interfaceManager = context.actorOf(Props[InterfaceManager], NamingSystem.InterfaceManager)
    private var areaLoaded: Area = _
    private val toServer: MessageDirection = Location.Admin >> Location.Master
    private val toSelf: MessageDirection = Location.Admin >> Location.Self

    override def init(args: List[Any]): Unit = {
        interfaceManager ! AriadneMessage(Init, Init.Subtype.Greetings, toSelf, Greetings(List.empty))
    }

    context.system.eventStream.subscribe(self, classOf[akka.remote.DisassociatedEvent])

    override def receptive: Receive = {
        //Ricezione del messaggio iniziale dall'interfaccia con aggiornamento iniziale
        case msg@AriadneMessage(_, Topology.Subtype.Planimetrics, _, area: Area) => {
            areaLoaded = area
            adminManager ! msg.copy(direction = toServer)
            context.become(operational)
            log.info("Map loaded from GUI")
        }

        case _ => desist _

    }

    def operational: Receive = {

        case msg@AriadneMessage(_, MessageType.Update.Subtype.Admin, _, adminUpdate: AdminUpdate) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => adminManager ! msg.copy(direction = toServer)

        case msg@AriadneMessage(_, Alarm.Subtype.FromCell, _, content: AlarmContent) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, sensorsInfo: SensorsInfoUpdate) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Error, Error.Subtype.MapIdentifierMismatch, _, _) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => adminManager ! msg.copy(direction = toServer)

        case event: akka.remote.DisassociatedEvent => {
            context.become(waitingForMaster)
            log.info("Lost connection with Master... Waiting for LookingForAMap message")
        }

        case _ => desist _

    }

    def waitingForMaster: Receive = {
        case msg@AriadneMessage(Error, Error.Subtype.LookingForAMap, _, _) => {
            adminManager = context.actorSelection("akka.tcp://Arianna-Cluster@127.0.0.1:25520/user/Master/AdminManager")
            log.info("Connection with Master established again")
            adminManager ! AriadneMessage(Topology, Topology.Subtype.Planimetrics, toServer, areaLoaded)
            log.info("Map sent again to Master")
            context.become(operational)
        }
        case msg => println(msg.toString)
    }

}
