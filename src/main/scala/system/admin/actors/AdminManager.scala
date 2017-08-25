package system.admin.actors

import akka.actor.{ActorSelection, Props}
import akka.extension.ConfigurationManager
import com.actors.TemplateActor
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Topology}
import system.ontologies.messages._

/**
  * This actor intermediates between the interface and the System. It sends the loaded map, handles the messages coming
  * from the System and communicates them to the InterfaceManager (in order to update the interface) and viceversa.
  * It keeps a copy of the loaded map if the System goes down and asks it again using a LookingForAMap message.
  */
class AdminManager extends TemplateActor {
    
    private val masterSeedNode = ConfigurationManager(context.system)
        .property("akka.cluster.seed-nodes").asStringList.head

    private val adminSupervisor: () => ActorSelection =
        () => context.actorSelection(masterSeedNode + "/user/" + NamingSystem.Master + "/" + NamingSystem.AdminSupervisor)
    
    private val interfaceManager = context.actorOf(Props[InterfaceManager], NamingSystem.InterfaceManager)
    private var areaLoaded: Area = _
    private val toMaster: MessageDirection = Location.Admin >> Location.Master
    private val toSelf: MessageDirection = Location.Admin >> Location.Self
    
    protected override def init(args: List[String]): Unit = {
        interfaceManager ! AriadneMessage(Init, Init.Subtype.Greetings, toSelf, Greetings(List.empty))
    }

    context.system.eventStream.subscribe(self, classOf[akka.remote.DisassociatedEvent])

    override def receptive: Receive = {
        case msg@AriadneMessage(_, Topology.Subtype.Planimetrics, _, area: Area) =>
            areaLoaded = area
            adminSupervisor() ! msg.copy(direction = toMaster)
            context.become(operational)
            log.info("Map loaded from GUI")


        case _ => desist _

    }

    def operational: Receive = {

        case msg@AriadneMessage(Topology, Topology.Subtype.Acknowledgement, _, _) =>
            log.info("ACK received from Master: Master connected")
            interfaceManager ! msg


        case msg@AriadneMessage(_, MessageType.Update.Subtype.Admin, _, _) => interfaceManager ! msg.copy(direction = toSelf)


        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => adminSupervisor() ! msg.copy(direction = toMaster)

        case msg@AriadneMessage(_, Alarm.Subtype.FromCell, _, _) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) => adminSupervisor() ! msg.copy(direction = toMaster)

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, _) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Error, Error.Subtype.MapIdentifierMismatch, _, _) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => adminSupervisor() ! msg.copy(direction = toMaster)

        case akka.remote.DisassociatedEvent =>
            interfaceManager ! AriadneMessage(Error, Error.Subtype.LostConnectionFromMaster, toSelf, Empty())
            context.become(waitingForMaster)
            log.info("Lost connection with Master... Waiting for LookingForAMap message")


        case _ => desist _

    }

    def waitingForMaster: Receive = {
        case AriadneMessage(Error, Error.Subtype.LookingForAMap, _, _) =>
            log.info("Connection with Master established again")
            adminSupervisor() ! AriadneMessage(Topology, Topology.Subtype.Planimetrics, toMaster, areaLoaded)
            log.info("Map sent again to Master")
            context.become(operational)

        case _ => desist _

    }

}
