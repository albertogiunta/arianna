package admin.actors

import akka.actor.{ActorSelection, Props}
import akka.extension.ConfigurationManager
import com.actors.TemplateActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Topology}
import ontologies.messages._
import system.names.NamingSystem

/**
  * This actor intermediates between the interface and the System. It sends the loaded map, handles the messages coming
  * from the System and communicates them to the InterfaceManager (in order to update the interface) and viceversa.
  * It keeps a copy of the loaded map if the System goes down and asks it again using a LookingForAMap message.
  */
class AdminManager extends TemplateActor {

    private val masterSeedNode = ConfigurationManager(context.system)
        .property(builder.akka.cluster.get("seed-nodes")).asStringList.head
    
    private val adminManager: () => ActorSelection =
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
        case msg@AriadneMessage(_, Topology.Subtype.Planimetrics, _, area: Area) => {
            areaLoaded = area
            adminManager() ! msg.copy(direction = toMaster)
            context.become(operational)
            log.info("Map loaded from GUI")
        }

        case _ => desist _

    }

    def operational: Receive = {

        case msg@AriadneMessage(_, MessageType.Update.Subtype.Admin, _, adminUpdate: AdminUpdate) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(_, Alarm.Subtype.FromInterface, _, _) => adminManager() ! msg.copy(direction = toMaster)

        case msg@AriadneMessage(_, Alarm.Subtype.FromCell, _, content: AlarmContent) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Alarm, Alarm.Subtype.End, _, _) => adminManager() ! msg.copy(direction = toMaster)

        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, sensorsInfo: SensorsInfoUpdate) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Error, Error.Subtype.MapIdentifierMismatch, _, _) => interfaceManager ! msg.copy(direction = toSelf)

        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => adminManager() ! msg.copy(direction = toMaster)

        case event: akka.remote.DisassociatedEvent => {
            context.become(waitingForMaster)
            log.info("Lost connection with Master... Waiting for LookingForAMap message")
        }

        case _ => desist _

    }

    def waitingForMaster: Receive = {
        case msg@AriadneMessage(Error, Error.Subtype.LookingForAMap, _, _) => {
            log.info("Connection with Master established again")
            adminManager() ! AriadneMessage(Topology, Topology.Subtype.Planimetrics, toMaster, areaLoaded)
            log.info("Map sent again to Master")
            context.become(operational)
        }
        case _ => desist _

    }

}
