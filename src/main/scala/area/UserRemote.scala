package area

import java.io.File
import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class UserRemote extends Actor with ActorLogging {

    // local actors
    val movementActor: ActorRef = context.actorOf(Props.create(classOf[MovementActor]), "movement")
    val switcherActor: ActorRef = context.actorOf(Props.create(classOf[MovementActor]), "switcher")

    // remote actors
    var cellAddress: String = "akka.tcp://cellSystem@127.0.0.1:4552/user/cell1"
    val cell: ActorSelection = context.actorSelection(cellAddress)

    var currentCell: CellForUser = _

    def operational: Receive = {
        case msg: Message.FromCell.ToUser.CELL_FOR_USER =>
            currentCell = msg.cell
            log.info("Received info from my new cell")
        case Message.FromUser.ToSelf.STOP =>
            cell ! Message.FromUser.ToCell.DISCONNECT
        case Message.FromCell.ToUser.ALARM =>
            log.info("received alarm")
        case msg: Message.FromUser.ToSelf.ASK_ROUTE =>
            cell ! Message.FromUser.ToCell.FIND_ROUTE(0, msg.toRoomId)
            log.info("asked cell for route to ID: {}", msg.toRoomId)
        case msg: Message.FromSwitcher.ToUser.SWITCH_CELL =>
            cellAddress = "akka.tcp://cellSystem@127.0.0.1:4552/user/" + msg.newCellInfo.uri
            cell ! Message.FromUser.ToCell.CONNECT
    }

    override def receive: Receive = {
        case Message.FromUser.ToSelf.START =>
            cell ! Message.FromUser.ToCell.CONNECT
            switcherActor ! Message.FromUser.ToSwitcher.SETUP_FIRST_ANTENNA_POSITION(Point(0, 0))
            movementActor ! Message.FromUser.ToMovement.START
            log.info("Asked cell for connection")
            context.become(operational)
    }
}

class MovementActor extends Actor with ActorLogging {

    // local actors
    val movementGeneratorActor: ActorRef = context.actorOf(Props.create(classOf[MovementGeneratorActor]), "movementGenerator")
    val powerSupplyActor: ActorSelection = context.actorSelection("../power")

    var currentUserPosition: Point = Point(0, 0)

    movementGeneratorActor ! Message.FromMovement.ToMovementGenerator.START

    override def receive: Receive = {
        case Message.FromUser.ToMovement.START => movementGeneratorActor ! Message.FromMovement.ToMovementGenerator.START
        case Message.FromMovementGenerator.ToMovement.UP =>
            log.info("Moved: UP")
            currentUserPosition.y += 1
            powerSupplyActor ! Message.FromMovement.ToSwitcher.NEW_USER_POSITION(currentUserPosition)
        case Message.FromMovementGenerator.ToMovement.DOWN =>
            log.info("Moved: DOWN")
            currentUserPosition.y -= 1
            powerSupplyActor ! Message.FromMovement.ToSwitcher.NEW_USER_POSITION(currentUserPosition)
        case Message.FromMovementGenerator.ToMovement.LEFT =>
            log.info("Moved: LEFT")
            currentUserPosition.x += 1
            powerSupplyActor ! Message.FromMovement.ToSwitcher.NEW_USER_POSITION(currentUserPosition)
        case Message.FromMovementGenerator.ToMovement.RIGHT =>
            log.info("Moved: RIGHT")
            currentUserPosition.x -= 1
            powerSupplyActor ! Message.FromMovement.ToSwitcher.NEW_USER_POSITION(currentUserPosition)
    }
}

class MovementGeneratorActor extends Actor with ActorLogging {

    override def receive: Receive = {
        case Message.FromMovement.ToMovementGenerator.START =>
            while (true) {
                Thread.sleep(1000)
                context.parent ! Message.FromMovementGenerator.ToMovement.UP
            }
    }
}

class SwitcherActor extends Actor with ActorLogging {

    val powerSupplyActor: ActorRef = context.actorOf(Props.create(classOf[PowerSupplyActor]), "power")

    var currentAntennaPosition: Point = _

    override def receive: Receive = {
        case msg: Message.FromUser.ToSwitcher.SETUP_FIRST_ANTENNA_POSITION =>
            currentAntennaPosition = msg.antennaPosition
        case msg: Message.FromUser.ToSwitcher.GET_BEST_NEW_CANDIDATE =>
            currentAntennaPosition = msg.bestCandidate.antennaPosition
            context.parent ! Message.FromSwitcher.ToUser.SWITCH_CELL(msg.bestCandidate)
        // TODO tell main user that will have to ask for all info about this new cell
        case msg: Message.FromMovement.ToSwitcher.NEW_USER_POSITION =>
            powerSupplyActor ! Message.FromSwitcher.ToPowerSupply.CALCULATE_STRENGTH_AFTER_POSITION_CHANGED(msg.userPosition, currentAntennaPosition)
        case Message.FromPowerSupply.ToSwitcher.SIGNAL_STRONG => log.info("Signal: STRONG")
        case Message.FromPowerSupply.ToSwitcher.SIGNAL_MEDIUM => log.info("Signal: MEDIUM")
        case Message.FromPowerSupply.ToSwitcher.SIGNAL_LOW => log.info("Signal: LOW")
            powerSupplyActor ! Message.FromSwitcher.ToPowerSupply.CONNECT_TO_CLOSEST_SOURCE
        case Message.FromPowerSupply.ToSwitcher.SIGNAL_ABSENT => log.info("Signal: ABSENT")
    }

}

class PowerSupplyActor extends Actor with ActorLogging {


    override def receive: Receive = {
        case msg: Message.FromSwitcher.ToPowerSupply.CALCULATE_STRENGTH_AFTER_POSITION_CHANGED =>
            context.parent ! getStrength(getDistanceFromSource(msg.userPosition, msg.antennaPosition))
        case msg: Message.FromSwitcher.ToPowerSupply.CONNECT_TO_CLOSEST_SOURCE =>
            context.parent ! Message.FromUser.ToSwitcher.GET_BEST_NEW_CANDIDATE(msg.antennaPositions.map(p => (p, getDistanceFromSource(msg.userPosition, p.antennaPosition))).minBy(_._2)._1)
    }

    def getDistanceFromSource(userPosition: Point, antennaPosition: Point): Double = Math.sqrt((userPosition.x - antennaPosition.x) + (userPosition.y - antennaPosition.y))

    def getStrength(distance: Double): String = {
        distance match {
            case _ if distance <= 3 =>
                Message.FromPowerSupply.ToSwitcher.SIGNAL_STRONG
            case _ if distance <= 6 =>
                Message.FromPowerSupply.ToSwitcher.SIGNAL_MEDIUM
            case _ if distance <= 9 =>
                Message.FromPowerSupply.ToSwitcher.SIGNAL_LOW
            case _ =>
                Message.FromPowerSupply.ToSwitcher.SIGNAL_ABSENT
        }
    }
}


object UserRun {
    def main(args: Array[String]): Unit = {
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/conf/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("userSystem", config.getConfig("user"))
        val userActor = system.actorOf(Props.create(classOf[UserRemote]), "user")
        userActor ! Message.FromUser.ToSelf.START
    }
}