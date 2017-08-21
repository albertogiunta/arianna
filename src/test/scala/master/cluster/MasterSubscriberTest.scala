package master.cluster

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.{ClusterMembersListener, CustomActor}
import com.typesafe.config.{Config, ConfigFactory}
import ontologies.messages.Location._
import ontologies.messages.MessageType.Handshake.Subtype.Acknowledgement
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import ontologies.messages.MessageType.{Handshake, Init, Route, Topology, Update}
import ontologies.messages._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem

import scala.io.Source
import scala.util.Random

/**
  * Created by Xander_C on 09/07/2017.
  */
@RunWith(classOf[JUnitRunner])
class MasterSubscriberTest extends TestKit(ActorSystem("SubscriberTest", MasterSubscriberTest.config))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    val mediator: ActorRef = DistributedPubSub(system).mediator
    
    val path2map: String = MasterSubscriberTest.path2Project + "/res/json/map4test.json"
    
    val plan: String = Source.fromFile(new File(path2map)).getLines.mkString
    
    val cellInfo = CellInfo(uri = "PancoPillo", port = 8080)
    
    val handshake = AriadneMessage(
        Handshake,
        Handshake.Subtype.CellToMaster,
        Location.Cell >> Location.Master,
        SensorsInfoUpdate(cellInfo, List(SensorInfo(1, 10.0)))
    )
    
    val ackTop = AriadneMessage(
        Topology,
        Topology.Subtype.Acknowledgement,
        Location.Cell >> Location.Master,
        cellInfo
    )
    
    val ackHand = AriadneMessage(Handshake, Acknowledgement, Location.Master >> Location.Cell, Empty())
    
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
        AreaViewedFromACell(planimetric.content.asInstanceOf[Area])
    )
    
    val updates = AriadneMessage(
        Update,
        Update.Subtype.CurrentPeople,
        Location.Cell >> Location.Master,
        CurrentPeopleUpdate(RoomID(serial = 777, name = "PancoPillo"), 0)
    )
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A Subscriber of the master node" should {
        
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
    
        "When Receptive, should wait for all the Subscrition ACK to arrive and become Subscribed " +
            " will wakes up its siblings when its Subscribed" in {
        
            probe.expectMsg(
                AriadneMessage(
                    Init, Init.Subtype.Greetings, Location.Cell >> Location.Self,
                    Greetings(List(ClusterMembersListener.greetings))
                )
            )
        
            probe.expectMsg(
                AriadneMessage(
                    Init, Init.Subtype.Greetings, Location.Cell >> Location.Self,
                    Greetings(List(ClusterMembersListener.greetings))
                )
            )
        }
    
        "When Subscribed stash all the Handshakes" in {
            
            tester ! handshake
    
            probe.expectMsg(ackHand)
    
            assert(probe.sender == tester.underlyingActor.publisher)
        }
        
        "Change its behaviour when a Planimetry is loaded, unstash all the handshakes" +
            " and Forward handshake requests to the TopologySupervisor, After becoming Sociable" in {
            
            tester ! planimetric
    
            probe.expectMsg(ackHand)
    
            assert(probe.sender == tester.underlyingActor.publisher)
            
            probe.expectMsg(handshake)
            
            assert(probe.sender == tester.underlyingActor.supervisor)
        }
        
        "Ignore everything that isn't an Handshake or an Update" in {
            tester ! AriadneMessage(
                Route,
                Route.Subtype.Request,
                Location.User >> Location.Cell,
                RouteRequest(Random.nextInt().toString, RoomID.empty, RoomID.empty, isEscape = false)
            )
            
            probe.expectNoMsg()
        }
        
        "Expect a Topology from the TopologySupervisor in order to change its behaviour after " +
            "sending off all the stashed and incoming handshakes" in {
            
            tester ! topologyViewedFromACell
            
            probe.expectMsg(topologyViewedFromACell)
            
            assert(probe.sender == tester.underlyingActor.publisher)
        }
    
        "Forward Updates,  and ACK to the TopologySupervisor" in {
            tester ! updates
        
            probe.expectMsg(updates)
        
            assert(probe.sender == tester.underlyingActor.supervisor)
        }
    
        "Forward ACK to the TopologySupervisor" in {
            tester ! ackTop
        
            probe.expectMsg(ackTop)
        
            assert(probe.sender == tester.underlyingActor.supervisor)
        }
    
        "Forward late handshakes to the TopologySupervisor" in {
            tester ! handshake
        
            probe.expectMsg(ackHand)
            probe.expectMsg(handshake)
    
            assert(probe.sender == tester.underlyingActor.supervisor)
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
    
        val subscriber: TestActorRef[MasterSubscriber] =
            TestActorRef(Props(new MasterSubscriber(mediator)), self, NamingSystem.Subscriber)
        
        val supervisor: ActorRef = context.actorOf(Props(new CustomActor {
            override def receive: Receive = {
                case msg => probe ! msg
            }
        }), NamingSystem.TopologySupervisor)
        
        val publisher: ActorRef = context.actorOf(Props(new CustomActor {
            override def receive: Receive = {
                case (_, msg) => probe ! msg
                case msg => probe ! msg
    
            }
        }), NamingSystem.Publisher)
        
        
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