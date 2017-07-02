package area

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class ServerRemote extends Actor with ActorLogging {

    import context._

    val nCells = 1

    def greetingCells: Receive = {
        case msg: Message.FromCell.ToServer.CELL_FOR_SERVER =>
            AreaLoader.area.cells += msg.cell
            log.info("Loaded new cell into area. ID: {}", msg.cell.infoCell.id)
            if (AreaLoader.area.cells.size == nCells) {
                log.info("Sending area for cell to each cell")
                AreaLoader.area.cells.foreach(c => actorSelection(c.infoCell.uri) ! AreaLoader.areaForCell)
                become(operational)
            }
    }

    def operational: Receive = {
        // TODO cases for sensors, people, alarm etc.
        case _ =>
    }

    override def receive: Receive = {
        case msg: Message.FromAdmin.ToServer.MAP_CONFIG =>
            AreaLoader.loadArea(msg.area)
            log.info("Loaded area")
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
        val config = ConfigFactory.parseFile(new File("src/main/scala/application.conf"))
        val system = ActorSystem.create("serverSystem", config.getConfig("server"))
        val server = system.actorOf(Props.create(classOf[ServerRemote]), "server")
        val notifier = system.actorOf(Props.create(classOf[ServerNotifier]), "notifier")
        server ! Message.FromServer.ToSelf.START

        while (true) {
            Thread.sleep(10)
            notifier ! Message.FromServer.ToNotifier.START
        }
    }
}