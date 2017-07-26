package master.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.actors.CustomActor
import com.typesafe.config.{Config, ConfigFactory}
import master.core.{DataStreamer, TopologySupervisor}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by Xander_C on 09/07/2017.
  */
class DataStreamerTest extends TestKit(ActorSystem("DataStreamerTest", DataStreamerTest.config))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    private class Tester(probe: ActorRef) extends CustomActor {
        
        val streamer: TestActorRef[TopologySupervisor] =
            TestActorRef(Props[DataStreamer], "DataStreamer")
        
        override def receive: Receive = {
            case msg if sender == streamer => probe forward msg
            case msg => streamer forward msg
        }
    }
    
}

object DataStreamerTest {
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2Config: String = path2Project + "/res/conf/akka/testMaster.conf"
    
    val config: Config = ConfigFactory.parseFile(new File(path2Config)).withFallback(ConfigFactory.load()).resolve()
}