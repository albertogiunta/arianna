package middleware

import java.io.File
import java.nio.file.Paths

import _root_.middleware.PublishSubscribeMiddleware.Message.{Publish, SubAck, Subscribe}
import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import com.actors.CustomActor
import com.typesafe.config.ConfigFactory
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake
import ontologies.messages.{AriadneMessage, Empty, Location}

object TryRemoteSelectionB extends App {
    
    val path2Project = Paths.get("").toFile.getAbsolutePath
    val path2Config = path2Project + "/res/conf/test/remoteOnlyB.conf"
    
    implicit val config = ConfigFactory.parseFile(new File(path2Config))
        .withFallback(ConfigFactory.load()).resolve()
    
    implicit val system = ActorSystem("Test", config)
    
    implicit val cluster = Cluster(system)
    
    val middleware = system.actorOf(Props[PublishSubscribeMiddleware], PublishSubscribeMiddleware.name)
    
    Thread.sleep(1000L)
    
    val actor = system.actorOf(Props(new CustomActor {
        
        override def preStart(): Unit = {
            super.preStart()
            middleware ! Subscribe("Topic/Num1", cluster.remotePathOf(self).toString)
            middleware ! Subscribe("Topic/Num2", cluster.remotePathOf(self).toString)
        }
        
        override def receive: Receive = {
            case SubAck(topic) => log.info("Found: {}", topic)
                if (topic == "Topic/Num1") middleware ! Publish(
                    "Topic/Num1",
                    AriadneMessage(Handshake, Handshake.Subtype.Acknowledgement, Location.Master >> Location.Cell, Empty())
                )
            case s: AriadneMessage[_] => log.info("Received " + s.toString + " from " + cluster.remotePathOf(sender))
            case _ =>
        }
    }), "PincoPallo")
}
