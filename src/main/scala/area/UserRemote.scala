package area

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class UserRemote extends Actor with ActorLogging {

    import context._

    val cellAddress: String = "akka.tcp://cellSystem@127.0.0.1:4552/user/cell1"
    val cell: ActorSelection = context.actorSelection(cellAddress)
    var currentCell: CellForUser = _

    def operational: Receive = {
        case Message.FromUser.ToSelf.STOP =>
            cell ! Message.FromUser.ToCell.DISCONNECT
        case Message.FromCell.ToUser.ALARM =>
            log.info("received alarm")
        // TODO make object for alarm with route to exit
        case msg: Message.FromUser.ToSelf.ASK_ROUTE =>
            cell ! Message.FromUser.ToCell.FIND_ROUTE(0, msg.toRoomId)
            log.info("asked cell for route to ID: {}", msg.toRoomId)
    }

    override def receive: Receive = {
        case Message.FromUser.ToSelf.START =>
            cell ! Message.FromUser.ToCell.CONNECT
            log.info("Asked cell for connection")
        case msg: Message.FromCell.ToUser.CELL_FOR_USER =>
            currentCell = msg.cell
            log.info("Received info from my cell")
            become(operational)
    }
}


object UserRun {
    def main(args: Array[String]): Unit = {
        val config = ConfigFactory.parseFile(new File("src/main/scala/application.conf"))
        val system = ActorSystem.create("userSystem", config.getConfig("user"))
        val user = system.actorOf(Props.create(classOf[UserRemote]), "user")
        user ! Message.FromUser.ToSelf.START
    }
}