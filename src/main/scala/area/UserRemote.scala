package area

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorLogging, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.{BasicActor, CustomActor}
import ontologies.messages._

class UserRemote extends BasicActor with ActorLogging {

    // local actors
    val movementActor: ActorRef = context.actorOf(Props.create(classOf[MovementActor]), "movement")
    val switcherActor: ActorRef = context.actorOf(Props.create(classOf[SwitcherActor]), "switcher")

    val fromCell2User: MessageDirection = Location.Cell >> Location.User
    val fromUser2Cell: MessageDirection = Location.User >> Location.Cell
    val fromUser2Switcher: MessageDirection = Location.User >> Location.Switcher

    // remote actors
    //    var cellAddress: String = "akka.tcp://cellSystem@127.0.0.1:4552/user/cell2"


    var currentCell: CellForUser = _

    override protected def init(args: List[Any]): Unit = {
        val cell: ActorSelection = context.actorSelection(args.head.asInstanceOf[String])
        cell ! AriadneRemoteMessage(MessageType.Handshake, MessageType.Handshake.Subtype.Cell2User, Location.User >> Location.Cell, "")
        log.info("Asked cell for connection")
    }

    override protected def receptive: Receive = {
        case AriadneRemoteMessage(MessageType.Handshake, MessageType.Handshake.Subtype.Cell2User, UserRemote.this.fromCell2User, obj) =>
            currentCell = MessageType.Handshake.Subtype.Cell2User.unmarshal(obj)
            log.info("Received info from my new cell of id: {}", currentCell.infoCell.name)
            movementActor ! AriadneLocalMessage(MessageType.Init, MessageType.Init.Subtype.Basic, Location.User >> Location.Movement, Point(15, 0))
            switcherActor ! AriadneLocalMessage(MessageType.Init, MessageType.Init.Subtype.Basic, Location.User >> Location.Switcher, CellForSwitcher(currentCell))
        case AriadneLocalMessage(MessageType.SwitcherMsg, MessageType.SwitcherMsg.Subtype.SwitchCell, _, newBestHost) =>
            newBestHost match {
                case c: InfoCell =>
                    val cellAddress: String = s"akka.tcp://cellSystem@127.0.0.1:4552/user/${c.name}"
                    init(List(cellAddress))
            }

        //        case Message.FromCell.ToUser.ALARM =>
        //            log.info("received alarm")
        //        case msg: Message.FromUser.ToSelf.ASK_ROUTE =>
        //            cell ! Message.FromUser.ToCell.FIND_ROUTE(0, msg.toRoomId)
        //            log.info("asked cell for route to ID: {}", msg.toRoomId)
    }
}

class MovementActor extends BasicActor with ActorLogging {

    // local actors
    val movementGeneratorActor: ActorRef = context.actorOf(Props.create(classOf[MovementGeneratorActor]), "movementGenerator")
    val switcherActor: Option[ActorSelection] = sibling("switcher")

    var currentUserPosition: Point = _

    override protected def init(args: List[Any]): Unit = {
        currentUserPosition match {
            case _ =>
                currentUserPosition = args.head.asInstanceOf[Point]
                movementGeneratorActor ! AriadneLocalMessage(MessageType.Init, MessageType.Init.Subtype.Basic, Location.Movement >> Location.MovementGenerator, "")
        }
    }

    override protected def receptive: Receive = {
        case AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Up, _, _) =>
            currentUserPosition.y += 1
            log.info("\tMoved:\tUP\t{}", currentUserPosition)
            sendUserPositionToPowerSupply()
        case AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Down, _, _) =>
            currentUserPosition.y -= 1
            log.info("\tMoved:\tDOWN\t{}", currentUserPosition)
            sendUserPositionToPowerSupply()
        case AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Left, _, _) =>
            currentUserPosition.x -= 1
            log.info("\tMoved:\tLEFT\t{}", currentUserPosition)
            sendUserPositionToPowerSupply()
        case AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Right, _, _) =>
            currentUserPosition.x += 1
            log.info("\tMoved:\tRIGHT\t{}", currentUserPosition)
            sendUserPositionToPowerSupply()
    }

    def sendUserPositionToPowerSupply(): Unit = {
        switcherActor.get ! AriadneLocalMessage(MessageType.Update, MessageType.Update.Subtype.UserPosition, Location.Movement >> Location.Switcher, currentUserPosition)
    }
}

class MovementGeneratorActor extends CustomActor with ActorLogging {

    override def receive: Receive = {
        case AriadneLocalMessage(MessageType.Init, MessageType.Init.Subtype.Basic, _, _) =>
            //            val random: Random = new Random()
            //            while (true) {

            log.info("FROM BORDER TO CENTER OF CELL2")
            for (i <- 1 to 5) {
                Thread.sleep(1000)
                context.parent ! AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Up, Location.MovementGenerator >> Location.Movement, "")
            }

            log.info("FROM CENTER TO BORDER OF CELL2")
            for (i <- 1 to 5) {
                Thread.sleep(1000)
                context.parent ! AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Left, Location.MovementGenerator >> Location.Movement, "")
            }

            log.info("FROM BORDER TO CENTER OF CELL1")
            for (i <- 1 to 5) {
                Thread.sleep(1000)
                context.parent ! AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Left, Location.MovementGenerator >> Location.Movement, "")
            }
        //                val randomInt = random.nextInt(3)
        //                randomInt match {
        //                    case 0 => context.parent ! AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Up, Location.MovementGenerator >> Location.Movement, "")
        //                    case 1 => context.parent ! AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Down, Location.MovementGenerator >> Location.Movement, "")
        //                    case 2 => context.parent ! AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Left, Location.MovementGenerator >> Location.Movement, "")
        //                    case 3 => context.parent ! AriadneLocalMessage(MessageType.Movement, MessageType.Movement.Subtype.Right, Location.MovementGenerator >> Location.Movement, "")
        //                }
        //            }
    }
}

class SwitcherActor extends BasicActor with ActorLogging {

    val powerSupplyActor: ActorRef = context.actorOf(Props.create(classOf[PowerSupplyActor]), "powerSupplyGenerator")
    powerSupplyActor ! AriadneLocalMessage(MessageType.Init, MessageType.Init.Subtype.Basic, Location.Switcher >> Location.PowerSupply, "")

    var currentInfoCell: CellForSwitcher = _
    var currentUserPosition: Point = _
    var bestNextHost: InfoCell = _
    //    val fromUser2Switcher: MessageDirection = Location.User >> Location.Switcher
    //    val fromMovement2Switcher: MessageDirection = Location.Movement >> Location.Switcher
    //    val fromPowerSupply2Switcher: MessageDirection = Location.Switcher << Location.PowerSupply
    val fromSwitcher2PowerSupply: MessageDirection = Location.Switcher >> Location.PowerSupply

    override protected def init(args: List[Any]): Unit = {
        log.info("Received antenna position")
        currentInfoCell = args.head.asInstanceOf[CellForSwitcher]
    }

    override protected def receptive: Receive = {
        case AriadneLocalMessage(MessageType.Init, MessageType.Init.Subtype.Basic, _, newInfoCell) =>
            newInfoCell match {
                case c: CellForSwitcher => currentInfoCell = c
            }
        case AriadneLocalMessage(MessageType.Update, MessageType.Update.Subtype.UserPosition, _, newUserPosition) =>
            newUserPosition match {
                case p: Point =>
                    currentUserPosition = p
                    powerSupplyActor ! AriadneLocalMessage(MessageType.SwitcherMsg, MessageType.SwitcherMsg.Subtype.CalculateStrength, Location.Switcher >> Location.PowerSupply, UserAndAntennaPositionUpdate(p, currentInfoCell.infoCell.antennaPosition))
            }
        case AriadneLocalMessage(MessageType.SwitcherMsg, MessageType.SwitcherMsg.Subtype.BestNexHost, _, bestNewCandidate) =>
            bestNewCandidate match {
                case p: InfoCell =>
                    bestNextHost = p
            }
        case AriadneLocalMessage(MessageType.SignalStrength, MessageType.SignalStrength.Subtype.Strong, _, _) =>
            log.info("\t\t\t\tSignal:\tSTRONG")
            bestNextHost = null
        case AriadneLocalMessage(MessageType.SignalStrength, MessageType.SignalStrength.Subtype.Medium, _, _) =>
            log.info("\t\t\t\tSignal:\tMEDIUM")
            bestNextHost = null
        case AriadneLocalMessage(MessageType.SignalStrength, MessageType.SignalStrength.Subtype.Low, _, _) =>
            log.info("\t\t\t\tSignal:\tLOW")
            powerSupplyActor ! AriadneLocalMessage(MessageType.SwitcherMsg, MessageType.SwitcherMsg.Subtype.ScanAndFind, SwitcherActor.this.fromSwitcher2PowerSupply, AntennaPositions(currentUserPosition, currentInfoCell.neighbors))
        case AriadneLocalMessage(MessageType.SignalStrength, MessageType.SignalStrength.Subtype.VeryLow, _, _) =>
            log.info("\t\t\t\tSignal:\tVERY LOW")
            parent ! AriadneLocalMessage(MessageType.SwitcherMsg, MessageType.SwitcherMsg.Subtype.SwitchCell, Location.Switcher >> Location.User, bestNextHost)
    }
}

class PowerSupplyActor extends BasicActor with ActorLogging {

    val fromSwitcher2PowerSupply: MessageDirection = Location.Switcher >> Location.PowerSupply

    override protected def init(args: List[Any]): Unit = {}

    override protected def receptive: Receive = {
        case AriadneLocalMessage(MessageType.SwitcherMsg, MessageType.SwitcherMsg.Subtype.CalculateStrength, _, userAndAntennaPosition) =>
            userAndAntennaPosition match {
                case p: UserAndAntennaPositionUpdate =>
                    parent ! getStrength(getDistanceFromSource(p.userPosition, p.antennaPosition))
            }
        case AriadneLocalMessage(MessageType.SwitcherMsg, MessageType.SwitcherMsg.Subtype.ScanAndFind, _, userAntennaPositions) =>
            userAntennaPositions match {
                case msg: AntennaPositions =>
                    val newBestCandidate = getNewBestCandidate(msg)
                    log.info("Found new best candidate: {}", newBestCandidate.name)
                    parent ! AriadneLocalMessage(MessageType.SwitcherMsg, MessageType.SwitcherMsg.Subtype.BestNexHost, Location.PowerSupply >> Location.Switcher, newBestCandidate)
            }
    }

    def getNewBestCandidate(msg: AntennaPositions): InfoCell = {
        msg.antennaPositions.map(p => (p, getDistanceFromSource(msg.userPosition, p.antennaPosition))).minBy(_._2)._1
    }

    def getDistanceFromSource(userPosition: Point, antennaPosition: Point): Double = {
        val distance = Math.sqrt(Math.pow(userPosition.x - antennaPosition.x, 2) + Math.pow(userPosition.y - antennaPosition.y, 2))
        log.info("\t\t\t\t\t\t{}\t{}\t{}", userPosition, antennaPosition, distance)
        distance
    }

    def getStrength(distance: Double): AriadneLocalMessage[String] = {
        distance match {
            case _ if distance <= 1 =>
                AriadneLocalMessage(MessageType.SignalStrength, MessageType.SignalStrength.Subtype.Strong, Location.PowerSupply >> Location.Switcher, "")
            case _ if distance <= 3 =>
                AriadneLocalMessage(MessageType.SignalStrength, MessageType.SignalStrength.Subtype.Medium, Location.PowerSupply >> Location.Switcher, "")
            case _ if distance <= 5 =>
                AriadneLocalMessage(MessageType.SignalStrength, MessageType.SignalStrength.Subtype.Low, Location.PowerSupply >> Location.Switcher, "")
            case _ =>
                AriadneLocalMessage(MessageType.SignalStrength, MessageType.SignalStrength.Subtype.VeryLow, Location.PowerSupply >> Location.Switcher, "")
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
        userActor ! AriadneLocalMessage(MessageType.Init, MessageType.Init.Subtype.Basic, Location.User >> Location.Self, "akka.tcp://cellSystem@127.0.0.1:4552/user/cell2")
    }
}