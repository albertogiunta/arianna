package similUser

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorLogging, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.BasicActor
import ontologies.messages.Location._
import ontologies.messages._
import spray.json.{DefaultJsonProtocol, _}

import scala.collection.mutable
import scala.io.Source

class CellRemote extends BasicActor with ActorLogging {

    val serverAddress: String = "akka.tcp://serverSystem@127.0.0.1:4553/user/server"
    val server: ActorSelection = context.actorSelection(serverAddress)
    
    var area: AreaViewedFromACell = _
    var cellSelfInfo: Cell = _
    val users: mutable.Set[ActorRef] = scala.collection.mutable.Set[ActorRef]()

    val fromUser2Cell: MessageDirection = Location.User >> Location.Cell
    val fromServer2Cell: MessageDirection = Location.Server >> Location.Cell

    def syncWithServer: Receive = {
        case msg: AreaViewedFromACell =>
            area = msg
            log.info("Received Area for Cell")
            context.become(receptive)
    }

    override protected def init(args: List[Any]): Unit = {
        cellSelfInfo = CellLoader.loadCell(args.head.asInstanceOf[String])
        log.info("Loaded Cell Self Info of {}", args.head.asInstanceOf[String])
        //        server ! AriadneMessage(MessageType.Handshake, MessageType.Handshake.Subtype.Cell2Master, Location.Cell >> Location.Server, MessageType.Handshake.Subtype.Cell2Master.marshal(cellSelfInfo.infoCell))
        //            server ! Message.FromCell.ToServer.CELL_FOR_SERVER(cellSelfInfo)
        //        context.become(syncWithServer)
        context.become(receptive)
    }

    override protected def receptive: Receive = {
        case AriadneMessage(MessageType.Handshake, MessageType.Handshake.Subtype.User2Cell, CellRemote.this.fromUser2Cell, _) =>
            users += sender()
            log.info("Connected new user")
            sender() ! AriadneMessage(MessageType.Handshake, MessageType.Handshake.Subtype.Cell2User, Location.Cell >> Location.User, CellForUser(cellSelfInfo, self.path.name))
        case AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.Basic, CellRemote.this.fromServer2Cell, _) =>
            users.foreach(u => u ! AriadneMessage(MessageType.Alarm, MessageType.Alarm.Subtype.Basic, Location.Cell >> Location.User, Empty()))
    }
}

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val pointFormat = jsonFormat2(Point)
    implicit val coordinatesFormat = jsonFormat4(Coordinates)
    implicit val infoCellFormat = jsonFormat5(InfoCell.apply)
    implicit val passageFormat = jsonFormat3(Passage)
    implicit val sensorFormat = jsonFormat4(Sensor)
    implicit val cellFormat = jsonFormat10(ontologies.messages.Cell)
}

import similUser.MyJsonProtocol._

object CellLoader {

    var area: Area = _

    private def readJson(filename: String): JsValue = {
        val source = Source.fromFile(filename).getLines.mkString
        source.parseJson
    }

    def loadCell(cellName: String): Cell = {
        val cell = readJson(s"res/json/cell/$cellName.json").convertTo[Cell]
        cell
    }
    
    def areaForCell: AreaViewedFromACell = {
        AreaViewedFromACell(area)
    }
}
object CellRun {
    def main(args: Array[String]): Unit = {
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("cellSystem", config.getConfig("cell"))
        val cell1 = system.actorOf(Props.create(classOf[CellRemote]), "cell1")
        cell1 ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.Cell >> Location.Self, Greetings(List("cell1")))
        val cell2 = system.actorOf(Props.create(classOf[CellRemote]), "cell2")
        cell2 ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.Cell >> Location.Self, Greetings(List("cell2")))
        val cell3 = system.actorOf(Props.create(classOf[CellRemote]), "cell3")
        cell3 ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.Cell >> Location.Self, Greetings(List("cell3")))
    }
}
