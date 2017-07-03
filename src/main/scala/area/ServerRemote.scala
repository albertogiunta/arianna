package area

import java.io.File
import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class ServerRemote extends Actor with ActorLogging {

    import context._

    val nCells = 1

    def greetingCells: Receive = {
        case msg: Message.FromCell.ToServer.CELL_FOR_SERVER =>
        //TODO "handshaking" with all cells and update of URI value in map
        /*AreaLoader.area.cells += msg.cell
        log.info("Loaded new cell into area. ID: {}", msg.cell.infoCell.id)
        if (AreaLoader.area.cells.size == nCells) {
            log.info("Sending area for cell to each cell")
            AreaLoader.area.cells.foreach(c => actorSelection(c.infoCell.uri) ! AreaLoader.areaForCell)
            become(operational)
        }*/
    }

    def operational: Receive = {
        // TODO cases for sensors, people, alarm etc.
        case Message.FromAdmin.ToServer.ALARM =>
            //TODO Reaction to alarm
            println("Alarm from Interface")
        case _ =>
    }

    override def receive: Receive = {
        case msg: Message.FromAdmin.ToServer.MAP_CONFIG =>
            println("Received map")
            println(msg.area.toString)
            become(greetingCells)
    }
}

class ServerNotifier extends Actor {
    override def receive: Receive = {
        case Message.FromServer.ToNotifier.START =>
            val adminRef = context.actorSelection("akka.tcp://adminSystem@127.0.0.1:4550/user/admin")
            adminRef ! Message.FromServer.ToAdmin.SAMPLE_UPDATE(SampleUpdate((Math.random() * 50).toInt, Math.random() * 15))
    }
}

object ServerRun {
    def main(args: Array[String]): Unit = {
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/conf/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("serverSystem", config.getConfig("server"))
        val server = system.actorOf(Props.create(classOf[ServerRemote]), "server")
        val notifier = system.actorOf(Props.create(classOf[ServerNotifier]), "notifier")
        server ! Message.FromServer.ToSelf.START

        while (true) {
            Thread.sleep(4000)
            notifier ! Message.FromServer.ToNotifier.START
        }
    }
}