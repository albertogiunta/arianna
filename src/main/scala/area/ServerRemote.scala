package area

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.CustomActor
import ontologies.messages._

class ServerRemote extends CustomActor {

    val toAdmin: MessageDirection = Location.Server >> Location.Admin
    val adminRef = context.actorSelection("akka.tcp://adminSystem@127.0.0.1:4550/user/admin")

    def operational: Receive = {
        case msg@AriadneLocalMessage(MessageType.Update, MessageType.Update.Subtype.UpdateForAdmin, _, _) => {
            val jsonUpdate = MessageType.Update.Subtype.UpdateForAdmin.marshal(msg.content.asInstanceOf[UpdateForAdmin])
            adminRef ! AriadneRemoteMessage(MessageType.Factory("Update"), MessageSubtype.Factory("UpdateForAdmin"), toAdmin, jsonUpdate)
        }
        case msg@AriadneLocalMessage(MessageType.Alarm, MessageType.Alarm.Subtype.Basic, _, _) => {
            adminRef ! AriadneRemoteMessage(MessageType.Factory("Alarm"), MessageSubtype.Factory("Alarm"), toAdmin, "")
        }
    }

    override def receive: Receive = {
        case msg@AriadneRemoteMessage(MessageType.Topology, MessageType.Topology.Subtype.Planimetrics, _, _) => {
            val area: Area = MessageType.Topology.Subtype.Planimetrics.unmarshal(msg.content)
            print(area.toString)
            //Invio all'attore che si occuperà di gestire la mappa
            context.become(operational)
        }

    }
}

object ServerRun {
    def main(args: Array[String]): Unit = {
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/conf/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("serverSystem", config.getConfig("server"))
        val server = system.actorOf(Props.create(classOf[ServerRemote]), "server")

        server ! "go"

        while (true) {
            Thread.sleep(4000)
            server ! "go"
        }
    }
}

/*val sensors : List[Sensor] = List(new Sensor(1, Math.random()*15), new Sensor(2, Math.random()*15))
            val curPeople : Int = (Math.random() * 50).toInt
            val coo : Coordinates = new Coordinates(Point(1,2), Point(3,4), Point(4,5), Point(8,9))
            val infoCell : InfoCell = new InfoCell(1,"uri", "cell1", coo, Point(3,3))
            val cellUpdate : CellUpdate = CellUpdate(infoCell, curPeople, sensors)
            //println(cellUpdate.toString)
            val jsonUpdate : String = MessageType.Update.Subtype.CellUpdate.marshal(cellUpdate)*/