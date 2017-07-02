package area

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class UserRemote extends Actor with ActorLogging {

    // local actors
    val movementActor: ActorRef = context.actorOf(Props.create(classOf[MovementActor]), "movement")
    val powerSupplyActor: ActorRef = context.actorOf(Props.create(classOf[PowerSupplyActor]), "power")

    // remote actors
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
        case Message.FromPowerSupply.ToUser.SIGNAL_STRONG => log.info("Signal: STRONG")
        case Message.FromPowerSupply.ToUser.SIGNAL_MEDIUM => log.info("Signal: MEDIUM")
        case Message.FromPowerSupply.ToUser.SIGNAL_LOW => log.info("Signal: LOW")
    }

    override def receive: Receive = {
        case Message.FromUser.ToSelf.START =>
            cell ! Message.FromUser.ToCell.CONNECT
            powerSupplyActor ! Message.FromUser.ToPowerSupply.CURRENT_ROOM_ANTENNA_POSITION(Point(0, 0))
            movementActor ! Message.FromUser.ToMovement.START
            log.info("Asked cell for connection")
        case msg: Message.FromCell.ToUser.CELL_FOR_USER =>
            currentCell = msg.cell
            log.info("Received info from my cell")
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
            powerSupplyActor ! Message.FromMovement.ToPowerSupply.NEW_USER_POSITION(currentUserPosition)
        case Message.FromMovementGenerator.ToMovement.DOWN =>
            log.info("Moved: DOWN")
            currentUserPosition.y -= 1
            powerSupplyActor ! Message.FromMovement.ToPowerSupply.NEW_USER_POSITION(currentUserPosition)
        case Message.FromMovementGenerator.ToMovement.LEFT =>
            log.info("Moved: LEFT")
            currentUserPosition.x += 1
            powerSupplyActor ! Message.FromMovement.ToPowerSupply.NEW_USER_POSITION(currentUserPosition)
        case Message.FromMovementGenerator.ToMovement.RIGHT =>
            log.info("Moved: RIGHT")
            currentUserPosition.x -= 1
            powerSupplyActor ! Message.FromMovement.ToPowerSupply.NEW_USER_POSITION(currentUserPosition)
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

class PowerSupplyActor extends Actor with ActorLogging {

    var currentAntennaPosition: Point = _

    override def receive: Receive = {
        case msg: Message.FromUser.ToPowerSupply.CURRENT_ROOM_ANTENNA_POSITION => currentAntennaPosition = msg.antennaPosition // TODO make different behaviour?
        case msg: Message.FromMovement.ToPowerSupply.NEW_USER_POSITION =>
            context.parent ! getStrength(getDistanceFromSource(msg.userPosition))
    }

    def getDistanceFromSource(userPosition: Point): Double = Math.sqrt((userPosition.x - currentAntennaPosition.x) + (userPosition.y - currentAntennaPosition.y))

    def getStrength(distance: Double): String = {
        distance match {
            case _ if distance <= 3 =>
                Message.FromPowerSupply.ToUser.SIGNAL_STRONG
            case _ if distance <= 6 =>
                Message.FromPowerSupply.ToUser.SIGNAL_MEDIUM
            case _ =>
                Message.FromPowerSupply.ToUser.SIGNAL_LOW
        }
    }
}


object UserRun {
    def main(args: Array[String]): Unit = {
        val config = ConfigFactory.parseFile(new File("src/main/scala/application.conf"))
        val system = ActorSystem.create("userSystem", config.getConfig("user"))
        val userActor = system.actorOf(Props.create(classOf[UserRemote]), "user")
        userActor ! Message.FromUser.ToSelf.START
    }
}