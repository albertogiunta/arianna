package master.core

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.CustomActor
import master.cluster.MasterPublisher
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init.Subtype
import ontologies.messages._

class AdminManager extends CustomActor {

    val toAdmin: MessageDirection = Location.Server >> Location.Admin
    val fromAdmin: MessageDirection = Location.Admin >> Location.Server
    val admin = context.actorSelection("akka.tcp://adminSystem@192.168.0.12:4550/user/admin")
    val subscriber: ActorSelection = sibling("Subscriber").get
    val publisher: ActorSelection = sibling("Publisher").get

    def operational: Receive = {
        //Ricezione di un update da qualcuno
        case msg@AriadneMessage(MessageType.Update, MessageType.Update.Subtype.UpdateForAdmin, _, jsonUpdate: UpdateForAdmin) => {
            println("ricevuto update adminmanager")
            admin ! AriadneMessage(MessageType.Update, MessageType.Update.Subtype.UpdateForAdmin, toAdmin, jsonUpdate)
        }
        //Ricezione di un allarme dall'admin
        case msg@AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.FromInterface, fromAdmin, _) => {
            println("Allarme ricevuto dal server")
            publisher ! AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.FromInterface, toAdmin, Empty())
        }
        //Ricezione di un allarme da parte del sistema
        case msg@AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.Basic, _, _) => {
            admin ! AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.Basic, toAdmin, Empty())
        }
    }

    override def receive: Receive = {
        case msg@AriadneMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, _, area: Area) => {
            subscriber ! msg
            println("ricevuta mappa")
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
             var sensors: ListBuffer[Sensor] = new ListBuffer[Sensor]
             for (i <- 0 until 5) {
                 sensors += Sensor(i, (Math.random() * 10).round.toDouble)
             }

             for (i <- 1 until 6) {
                 update += new CellUpdate(new InfoCell(i, "uri0", "a", new Coordinates(Point(1, 1), Point(1, 1), Point(1, 1), Point(1, 1)), Point(1, 1)), (Math.random() * 50).round.toInt, sensors.toList)
             }
             server ! AriadneMessage(MessageType.Update, MessageType.Update.Subtype.UpdateForAdmin, Location.Server >> Location.Self,
                 new UpdateForAdmin(update.toList))
         }*/
    }

}

/*val sensors : List[Sensor] = List(new Sensor(1, Math.random()*15), new Sensor(2, Math.random()*15))
            val curPeople : Int = (Math.random() * 50).toInt
            val coo : Coordinates = new Coordinates(Point(1,2), Point(3,4), Point(4,5), Point(8,9))
            val infoCell : InfoCell = new InfoCell(1,"uri", "cell1", coo, Point(3,3))
            val cellUpdate : CellUpdate = CellUpdate(infoCell, curPeople, sensors)
            //println(cellUpdate.toString)
            val jsonUpdate : String = MessageType.Update.Subtype.CellUpdate.marshal(cellUpdate)*/