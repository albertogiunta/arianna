package cell.core

import _root_.io.vertx.core.Vertx
import akka.actor.ActorLogging
import common.BasicActor
import ontologies.messages.AriannaJsonProtocol._
import ontologies.messages.MessageType.Topology
import ontologies.messages.MessageType.Topology.Subtype.ViewedFromACell
import ontologies.messages._
import similUser.WSClient
import spray.json._


class UserManager extends BasicActor with ActorLogging {

    var vertx: Vertx = Vertx.vertx
    var s = new WSServer(vertx, self)
    var usrNumber = 0
    var area: AreaViewedFromACell = _
    var c = new WSClient(vertx)

    override protected def init(args: List[Any]): Unit = {
        log.info("Started actor")
        vertx.deployVerticle(s)
        //        initWSClient()
    }

    def initWSClient(): Unit = {
        vertx.deployVerticle(c)
        Thread.sleep(3000)
        c.sendMessageConnect()
    }

    override protected def receptive: Receive = {
        case msg@AriadneMessage(Topology, ViewedFromACell, _, area: AreaViewedFromACell) =>
            println("sono receptive")
            this.area = area
            context.become(receptiveForMobile)
            println("in teoria sono receptiveforuser")
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
            println(s"Area received from the Cell Core $area")
            s.sendAreaToNewUser(area.toJson.toString())
            usrNumber += 1
        case _ => ""
    }
}


/*object UserRun {
    def main(args: Array[String]): Unit = {
        val path2Project = Paths.get("").toFile.getAbsolutePath
        val path2Config = path2Project + "/res/conf/akka/application.conf"
        val config = ConfigFactory.parseFile(new File(path2Config))
        val system = ActorSystem.create("userSystem", config.getConfig("similUser"))
        val userActor = system.actorOf(Props.create(classOf[UserActor]), "similUser")
        userActor ! AriadneMessage(MessageType.Init, MessageType.Init.Subtype.Greetings, Location.User >> Location.Self, Greetings(List.empty))
    }
}*/