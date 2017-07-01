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
        // TODO cases for sensors, people etc.
        case _ =>
    }

    override def receive: Receive = {
        case Message.FromServer.ToSelf.START =>
            AreaLoader.loadArea
            log.info("Loaded area")
            become(greetingCells)
    }
}


object ServerRun {
    def main(args: Array[String]): Unit = {
        val config = ConfigFactory.parseFile(new File("src/main/scala/application.conf"))
        val system = ActorSystem.create("serverSystem", config.getConfig("server"))
        val server = system.actorOf(Props.create(classOf[ServerRemote]), "server")
        server ! Message.FromServer.ToSelf.START
    }
}