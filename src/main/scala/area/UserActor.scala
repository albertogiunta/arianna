package area

import java.io.File
import java.nio.file.Paths

import _root_.io.vertx.core.Vertx
import akka.actor.{ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import common.BasicActor
import ontologies.messages.Location._
import ontologies.messages._


class UserActor extends BasicActor with ActorLogging {

    var vertx: Vertx = Vertx.vertx
    var s = new WSServer(vertx, UserActor.this.self)
    var usrNumber = 0
    //    var c = new WSClient(vertx)

    override protected def init(args: List[Any]): Unit = {
        log.info("Started actor")
        vertx.deployVerticle(s)
        //        vertx.deployVerticle(c)
        //        Thread.sleep(3000)
        //        c.sendMessageConnect()
    }

    // TODO become handshaking col server

    override protected def receptive: Receive = {
        case "areafrom" => context.become(receptiveForMobile)
        case "connect" =>
            println("[ACTOR] GOT NEW USER")
            s.sendOkToNewUser()
            usrNumber += 1
        case "disconnect" =>
            println("[ACTOR] USER DISCONNECTING")
            usrNumber -= 1
        case "firstconnection" =>
            println("[ACTOR] GOT NEW FIRST USER")
            s.sendAreaToNewUser("Area to new user")
            usrNumber += 1
        case _ => ""
    }

    protected def receptiveForMobile: Receive = {
        case "connect" =>
            println("[ACTOR] GOT NEW USER")
            s.sendOkToNewUser()
            usrNumber += 1
        case "disconnect" =>
            println("[ACTOR] USER DISCONNECTING")
            usrNumber -= 1
        case "firstconnection" =>
            println("[ACTOR] GOT NEW FIRST USER")
            s.sendAreaToNewUser("Area to new user")
            usrNumber += 1
        case _ => ""
    }
}


object UserRun {
    def main(args: Array[String]): Unit = {
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("userSystem", config.getConfig("user"))
        val userActor = system.actorOf(Props.create(classOf[UserActor]), "user")
        userActor ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.User >> Location.Self, Greetings(List.empty))
    }
}