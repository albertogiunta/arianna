package master.tests

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import com.typesafe.config.{Config, ConfigFactory}
import master.cluster.MasterSubscriber
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import ontologies.messages.MessageType.{Handshake, Init, Route, Topology, Update}
import ontologies.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.io.Source
import scala.util.Random

/**
  * Created by Xander_C on 09/07/2017.
  */
class MasterSubscriberTest extends TestKit(ActorSystem("SubscriberTest", MasterSubscriberTest.config))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    val handshake = AriadneMessage(
        Handshake,
        Handshake.Subtype.CellToMaster,
        Location.Cell >> Location.Master,
        SensorsUpdate(
            InfoCell.empty,
            List(Sensor(0, 0.0, 0.0, 0.0))
        )
    )
    
    val path2map: String = MasterSubscriberTest.path2Project + "/res/json/map4test.json"
    
    val plan: String = Source.fromFile(new File(path2map)).getLines.mkString
    
    val planimetric = AriadneMessage(
        Topology,
        Topology.Subtype.Planimetrics,
        Location.Admin >> Location.Master,
        Planimetrics.unmarshal(plan)
    )
    
    val topologyViewedFromACell = AriadneMessage(
        Topology,
        ViewedFromACell,
        Location.Master >> Location.Cell,
        AreaViewedFromACell(planimetric.content)
    )
    
    val updates = AriadneMessage(
        Update,
        Update.Subtype.CurrentPeople,
        Location.Cell >> Location.Master,
        CurrentPeopleUpdate(InfoCell.empty, 0)
    )
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A Subscriber of the master node" should {
        
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
        
        "Initially stash all the Handshakes" in {
            
            tester ! handshake
            
            probe.expectNoMsg()
            
        }
        
        "Change its behaviour when a Planimetry is loaded, unstash all the handshakes" +
            " and Forward handshake requests to the TopologySupervisor, After becoming Sociable" in {
            
            tester ! planimetric
            
            probe.expectMsg(handshake)
            
            assert(probe.sender == tester.underlyingActor.supervisor)
        }
        
        "Ignore everything that isn't an Handshake or an Update" in {
            tester ! AriadneMessage(
                Route,
                Route.Subtype.Request,
                Location.User >> Location.Cell,
                RouteRequest(Random.nextInt().toString, InfoCell.empty, InfoCell.empty, isEscape = false)
            )
            
            probe.expectNoMsg()
        }
        
        "Expect a Topology from the TopologySupervisor in order to change its behaviour after " +
            "sending off all the stashed and incoming handshakes" in {
            
            tester ! topologyViewedFromACell
            
            probe.expectMsg(topologyViewedFromACell)
            
            assert(probe.sender == tester.underlyingActor.publisher)
        }
        
        "Forward Updates and late handshakes to the TopologySupervisor" in {
            tester ! updates
            
            probe.expectMsg(updates)
            
            assert(probe.sender == tester.underlyingActor.supervisor)
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
        
        val subscriber: TestActorRef[MasterSubscriber] = TestActorRef(Props[MasterSubscriber], self, "Subscriber")
        
        val supervisor: ActorRef = context.actorOf(Props(new CustomActor {
            override def receive: Receive = {
                case msg => probe ! msg
            }
        }), "TopologySupervisor")
        
        val publisher: ActorRef = context.actorOf(Props(new CustomActor {
            override def receive: Receive = {
                case msg => probe ! msg
            }
        }), "Publisher")
        
        
        override def preStart {
            
            subscriber ! AriadneMessage(
                Init,
                Init.Subtype.Greetings,
                Location.Cell >> Location.Self,
                Greetings(List.empty)
            )
        }
        
        override def receive: Receive = {
            case msg if sender == subscriber => probe forward msg
            case msg => subscriber forward msg
        }
    }
    
}


object MasterSubscriberTest {
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2Config: String = path2Project + "/res/conf/akka/testMaster.conf"
    
    val config: Config = ConfigFactory.parseFile(new File(path2Config)).withFallback(ConfigFactory.load()).resolve()
}