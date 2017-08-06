package master.core

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSelection, ActorSystem, Props}
import com.actors.CustomActor
import com.typesafe.config.ConfigFactory
import master.cluster.MasterPublisher
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init.Subtype
import ontologies.messages.MessageType.{Alarm, Handshake, Init, Update}
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
            println("Ricevuta mappa")
            context.become(operational)
        }
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


        /* while (true) {
             Thread.sleep(1000)
             var update: ListBuffer[CellUpdate] = new ListBuffer[CellUpdate]
             var sensors: ListBuffer[SensorInfo] = new ListBuffer[SensorInfo]
             for (i <- 0 until 5) {
                 sensors += Sensor(i, (Math.random() * 10).round.toDouble)
             }

             for (i <- 1 until 6) {
                 update += new CellUpdate(new InfoCell(i, "uri0", "a", new Coordinates(Point(1, 1), Point(1, 1), Point(1, 1), Point(1, 1)), Point(1, 1)), (Math.random() * 50).round.toInt, sensors.toList)
             }
             server ! AriadneMessage(MessageType.Update, MessageType.Update.Subtype.UpdateForAdmin, Location.Master >> Location.Self,
                 new UpdateForAdmin(update.toList))
         }*/
    }

}

/*val sensors : List[SensorInfo] = List(new Sensor(1, Math.random()*15), new Sensor(2, Math.random()*15))
            val curPeople : Int = (Math.random() * 50).toInt
            val coo : Coordinates = new Coordinates(Point(1,2), Point(3,4), Point(4,5), Point(8,9))
            val infoCell : InfoCell = new InfoCell(1,"uri", "cell1", coo, Point(3,3))
            val cellUpdate : CellUpdate = CellUpdate(infoCell, curPeople, sensors)
            //println(cellUpdate.toString)
            val jsonUpdate : String = MessageType.Update.Subtype.CellUpdate.marshal(cellUpdate)*/