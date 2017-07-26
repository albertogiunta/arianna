package master.core

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.actors.CustomActor
import com.typesafe.config.{Config, ConfigFactory}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Init
import ontologies.messages.{AriadneMessage, Greetings, Location}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by Xander_C on 09/07/2017.
  */
class TopologySupervisorTest extends TestKit(ActorSystem("TopologySupervisorTest", TopologySupervisorTest.config))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    
    private class Tester(probe: ActorRef) extends CustomActor {
        
        val supervisor: TestActorRef[TopologySupervisor] =
            TestActorRef(Props[TopologySupervisor], "TopologySupervisor")
        
        override def preStart {
            
            supervisor ! AriadneMessage(
                Init,
                Init.Subtype.Greetings,
                Location.Cell >> Location.Self,
                Greetings(List.empty)
            )
        }
        
        override def receive: Receive = {
            case msg if sender == supervisor => probe forward msg
            case msg => supervisor forward msg
        }
    }
    
}

object TopologySupervisorTest {
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2Config: String = path2Project + "/res/conf/akka/testMaster.conf"
    
    val config: Config = ConfigFactory.parseFile(new File(path2Config)).withFallback(ConfigFactory.load()).resolve()
}
