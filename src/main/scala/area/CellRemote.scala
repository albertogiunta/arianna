package area

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import spray.json.{DefaultJsonProtocol, _}

import scala.collection.mutable
import scala.io.Source

class CellRemote extends Actor with ActorLogging {

    import context._

    val serverAddress: String = "akka.tcp://serverSystem@127.0.0.1:4553/user/server"
    val server: ActorSelection = context.actorSelection(serverAddress)

    var area: AreaForCell = _
    var cellSelfInfo: Cell = _
    val users: mutable.Set[ActorRef] = scala.collection.mutable.Set[ActorRef]()

    def syncWithServer: Receive = {
        case msg: AreaForCell =>
            area = msg
            log.info("Received Area for Cell")
            become(operational)
    }

    def operational: Receive = {
        case Message.FromUser.ToCell.CONNECT =>
            users += sender()
            log.info("Connected new user")
            sender() ! Message.FromCell.ToUser.CELL_FOR_USER(CellForUser(cellSelfInfo, self))
        case Message.FromUser.ToCell.DISCONNECT =>
            users.remove(sender())
            log.info("Removed user")
        case Message.FromUser.ToCell.FIND_ROUTE => // TODO calculate route
        case Message.FromServer.ToCell.SEND_ALARM_TO_USERS => users.foreach(u => u ! Message.FromCell.ToUser.ALARM)
    }

    override def receive: Receive = {
        case Message.FromCell.ToSelf.START =>
            cellSelfInfo = CellLoader.loadCell("cell1")
            log.info("Loaded Cell Self Info")
            server ! Message.FromCell.ToServer.CELL_FOR_SERVER(cellSelfInfo)
            become(syncWithServer)
    }
}

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val pointFormat = jsonFormat2(Point)
    implicit val coordinatesFormat = jsonFormat4(Coordinates)
    implicit val infoCellFormat = jsonFormat5(InfoCell)
    implicit val passageFormat = jsonFormat3(Passage)
    implicit val sensorFormat = jsonFormat2(Sensor)
    implicit val cellFormat = jsonFormat10(Cell)
}

import area.MyJsonProtocol._

object CellLoader {

    var area: Area = null

    private def readJson(filename: String): JsValue = {
        val source = Source.fromFile(filename).getLines.mkString
        source.parseJson
    }

    def loadCell(cellName: String): Cell = {
        val cell = readJson(s"res/json/cell/$cellName.json").convertTo[Cell]
        cell
    }

    def areaForCell: AreaForCell = {
        AreaForCell(area)
    }
}
object CellRun {
    def main(args: Array[String]): Unit = {
        val config = ConfigFactory.parseFile(new File("src/main/scala/application.conf"))
        val system = ActorSystem.create("cellSystem", config.getConfig("cell"))
        val cell = system.actorOf(Props.create(classOf[CellRemote]), "cell1")
        cell ! Message.FromCell.ToSelf.START
    }
}
