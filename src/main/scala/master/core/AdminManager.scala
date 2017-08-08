package master.core

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSelection, ActorSystem, Props}
import com.actors.CustomActor
import com.typesafe.config.ConfigFactory
import master.cluster.MasterPublisher
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init.Subtype
import ontologies.messages.MessageType.{Alarm, Error, Handshake, Init, Update}
import ontologies.messages._

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
        case msg@AriadneMessage(Update, Update.Subtype.Admin, _, _) => admin ! msg.copy(direction = toAdmin)
        //Ricezione di un allarme dall'admin
        case msg@AriadneMessage(Alarm, Alarm.Subtype.FromInterface, fromAdmin, _) => publisher ! msg.copy(direction = fromAdmin)
        //Ricezione di un allarme da parte del sistema
        case msg@AriadneMessage(Alarm, Alarm.Subtype.FromCell, _, _) => admin ! msg.copy(direction = toAdmin)
        //Ricezione di aggiornamento sensori
        case msg@AriadneMessage(Handshake, Handshake.Subtype.CellToMaster, _, _) => admin ! msg

        case msg@AriadneMessage(Init, Init.Subtype.Goodbyes, _, _) => parent ! msg.copy(direction = fromAdmin)

    }

    override def receive: Receive = {
        case msg@AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, _, area: Area) => {
            topologySupervisor ! msg
            log.info("Map received from Admin")
            context.become(operational)
        }
        case msg@AriadneMessage(Error, Error.Subtype.LookingForAMap, _, _) => admin ! msg

    }
}

object ServerRun {
    def main(args: Array[String]): Unit = {
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/testMaster.conf"
        //val path2Config2 = path2Project + "/res/conf/akka/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
            .withFallback(ConfigFactory.load()).resolve()
        
        val system = ActorSystem.create("Arianna-Cluster", config)
    
        val server = system.actorOf(Props[AdminManager], "AdminManager")
        val publisher = system.actorOf(Props[MasterPublisher], "Publisher")

        val alarmSup = system.actorOf(Props[AlarmSupervisor], "AlarmSupervisor")

        publisher ! AriadneMessage(MessageType.Init, Subtype.Greetings, Location.Self >> Location.Self, Greetings(List.empty));

    }

}