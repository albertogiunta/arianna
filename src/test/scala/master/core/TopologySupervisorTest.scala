package master.core

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import com.utils.Practicability
import ontologies.messages.Location._
import ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import ontologies.messages.MessageType.{Error, Handshake, Init, Topology, Update}
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
    
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2map: String = path2Project + "/res/json/map4test.json"
    
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
    
    val cellInfo = CellInfo(uri = "PancoPillo", port = 8080)
    
    val handshake = AriadneMessage(
        Handshake,
        Handshake.Subtype.CellToMaster,
        Location.Cell >> Location.Master,
        SensorsInfoUpdate(cellInfo, List(SensorInfo(1, 10.0)))
    )
    
    val currentPeopleUpdate = AriadneMessage(
        Update,
        Update.Subtype.CurrentPeople,
        Location.Cell >> Location.Master,
        CurrentPeopleUpdate(RoomID(serial = 666, name = "PancoPillo"), 666)
    )
    
    val sensorsUpdate = AriadneMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Master,
        SensorsInfoUpdate(cellInfo, List(SensorInfo(1, 10.0)))
    )
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A TopologySupervisor" should {
        
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
    
        "At start time" must {
            "lack of the map and notify the admin" in {
                probe.expectMsg(
                    AriadneMessage(
                        Error, Error.Subtype.LookingForAMap,
                        Location.Master >> Location.Admin, Empty()
                    )
                )
            }
        }
        
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
                assert(probe.sender == tester.underlyingActor.admin)
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
    
                val topology = mutable.HashMap(planimetric.content.rooms.map(r => r.cell.info.uri -> r): _*)
    
                val newCell = topology(handshake.content.cell.uri).cell
                    .copy(
                        info = handshake.content.cell,
                        sensors = sensorsUpdate.content.sensors
                    )
    
                val newRoom = topology(handshake.content.cell.uri).copy(cell = newCell)
    
                topology.put(handshake.content.cell.uri, newRoom)
                tester ! sensorsUpdate
                probe.expectMsg(AdminUpdate(0, topology.values.map(c => RoomDataUpdate(c)).toList))
            }
            
            "accept new current people" in {
    
                val topology: mutable.Map[String, Room] =
                    mutable.HashMap(planimetric.content
                        .rooms.map(room => room.cell.info.uri -> room): _*)
    
                val oldRoom = topology(handshake.content.cell.uri)
    
                val newRoom: Room = topology(handshake.content.cell.uri)
                    .copy(
                        cell = ontologies.messages.Cell(handshake.content.cell, sensorsUpdate.content.sensors),
                        currentPeople = currentPeopleUpdate.content.currentPeople,
                        practicability = Practicability(
                            oldRoom.info.capacity,
                            currentPeopleUpdate.content.currentPeople,
                            oldRoom.passages.length
                        )
                    )
    
                topology.put(handshake.content.cell.uri, newRoom)
                
                tester ! currentPeopleUpdate
    
                probe.expectMsg(AdminUpdate(0, topology.values.map(c => RoomDataUpdate(c)).toList))
            }
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
        
        val supervisor: TestActorRef[TopologySupervisor] =
            TestActorRef(Props[TopologySupervisor], self, NamingSystem.TopologySupervisor)
    
        val publisher: TestActorRef[CustomActor] = TestActorRef(Props(new CustomActor {
            override def receive: Receive = {
                case msg: AriadneMessage[_] => probe ! msg
                case (_: String, cnt: AriadneMessage[_]) => probe ! cnt
            }
        }), self, NamingSystem.Publisher)
    
        val subscriber: TestActorRef[CustomActor] = TestActorRef(Props(new CustomActor {
            override def receive: Receive = {
                case msg => probe ! msg
            }
        }), self, NamingSystem.Subscriber)
    
        val admin: TestActorRef[CustomActor] = TestActorRef(Props(new CustomActor {
            override def receive: Receive = {
                case AriadneMessage(Update, Update.Subtype.Admin, _, cnt: AdminUpdate) =>
                    probe ! cnt
                case msg => probe ! msg
            }
        }), self, NamingSystem.AdminManager)
        
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
