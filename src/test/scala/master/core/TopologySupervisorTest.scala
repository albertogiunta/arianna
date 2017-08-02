package master.core

import java.io.File

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import com.utils.Practicability
import master.cluster.MasterSubscriberTest
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import ontologies.messages.MessageType.{Handshake, Init, Topology, Update}
import ontologies.messages._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem

import scala.collection.mutable
import scala.io.Source

/**
  * Created by Xander_C on 09/07/2017.
  */
@RunWith(classOf[JUnitRunner])
class TopologySupervisorTest extends TestKit(ActorSystem("TopologySupervisorTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
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

    val infoCell = InfoCell(id = 666, uri = "PancoPillo:8080", 0, name = "PancoPillo", roomVertices = Coordinates(Point(100, 100), Point(250, 100), Point(100, 200), Point(250, 200)), antennaPosition = Point(175, 150))
    
    val handshake = AriadneMessage(
        Handshake,
        Handshake.Subtype.CellToMaster,
        Location.Cell >> Location.Master,
        SensorsInfoUpdate(infoCell, List(SensorInfo(1, 10.0)))
    )
    
    val currentPeopleUpdate = AriadneMessage(
        Update,
        Update.Subtype.CurrentPeople,
        Location.Cell >> Location.Master,
        CurrentPeopleUpdate(infoCell, 666)
    )
    
    val sensorsUpdate = AriadneMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Master,
        SensorsInfoUpdate(infoCell, List(SensorInfo(1, 10.0)))
    )
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A TopologySupervisor" should {
        
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
        
        "When receptive" must {
            
            "stash anything that is not a Planimetry" in {
                tester ! "Ciaone"
                probe.expectNoMsg()
            }
            
            "wait for the Planimetry, unstashing messages and becoming sociable" in {
                tester ! planimetric
                probe.expectMsg(planimetric)
                assert(probe.sender == tester.underlyingActor.subscriber)
            }
        }
        
        
        "When sociable" must {
            
            "wait for the Handshakes to be forwarded" in {
                tester ! handshake
                probe.expectMsg(handshake)
                assert(probe.sender == tester.underlyingActor.streamer)
            }
            
            "once all the expected handshakes have been mapped, should notify the subscriber, sending the Topology and becoming proactive" in {
                probe.expectMsg(topologyViewedFromACell)
                assert(probe.sender == tester.underlyingActor.subscriber)
            }
        }
        
        
        "When proactive" must {
            
            "accept late handshakes" in {
                tester ! handshake
                probe.expectMsg(topologyViewedFromACell)
                assert(probe.sender == tester.underlyingActor.publisher)
            }
            
            "accept new sensors values" in {
                
                val topology = mutable.HashMap(planimetric.content.cells.map(c => c.info.uri -> c): _*)
                val news = topology(handshake.content.info.uri)
                    .copy(info = handshake.content.info,
                        sensors = sensorsUpdate.content.sensors
                    )
                
                topology.put(handshake.content.info.uri, news)
                tester ! sensorsUpdate
                probe.expectMsg(topology.values.toList)
            }
            
            "accept new current people" in {
                val topology: mutable.Map[String, Cell] =
                    mutable.HashMap(planimetric.content.cells.map(c => c.info.uri -> c): _*)
                val old = topology(handshake.content.info.uri)
                val news: Cell = topology(handshake.content.info.uri)
                    .copy(
                        info = handshake.content.info,
                        currentPeople = currentPeopleUpdate.content.currentPeople,
                        sensors = sensorsUpdate.content.sensors,
                        practicability = Practicability(old.capacity, currentPeopleUpdate.content.currentPeople, old.passages.length)
                    )
                
                topology.put(handshake.content.info.uri, news)
                
                tester ! currentPeopleUpdate
                probe.expectMsg(topology.values.toList)
            }
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
        
        val supervisor: TestActorRef[TopologySupervisor] =
            TestActorRef(Props[TopologySupervisor], self, NamingSystem.TopologySupervisor)
    
        val publisher: TestActorRef[CustomActor] = TestActorRef(Props(new CustomActor {
            override def receive: Receive = {
                case msg: AriadneMessage[_] => probe ! msg
                case msg@(dest: String, cnt: AriadneMessage[_]) => probe ! cnt
            }
        }), self, NamingSystem.Publisher)
    
        val subscriber: TestActorRef[CustomActor] = TestActorRef(Props(new CustomActor {
            override def receive: Receive = {
                case msg => probe ! msg
            }
        }), self, NamingSystem.Subscriber)
    
        val streamer: TestActorRef[CustomActor] = TestActorRef(Props(new CustomActor {
            override def receive: Receive = {
                case msg: Iterable[_] => probe ! msg.toList
                case msg => probe ! msg
            }
        }), self, NamingSystem.DataStreamer)
        
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
