package system.master.core

import java.io.File
import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import com.utils.Practicability
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.master.cluster.MasterSubscriber
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Topology.Subtype.{Planimetrics, ViewedFromACell}
import system.ontologies.messages.MessageType.{Error, Handshake, Init, Topology, Update}
import system.ontologies.messages.{Cell, _}

import scala.collection.mutable
import scala.io.Source

/**
  * Created by Xander_C on 09/07/2017.
  */
class TopologySupervisorTest extends TestKit(ActorSystem("TopologySupervisorTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    val path2Project: String = Paths.get("").toFile.getAbsolutePath
    val path2map: String = path2Project + "/res/json/map4test.json"
    
    val cellInfo = CellInfo(uri = "PancoPillo", port = 8080)
    val plan: Area = Planimetrics.unmarshal(Source.fromFile(new File(path2map)).getLines.mkString)
    val sensorsInfo = SensorsInfoUpdate(cellInfo, List(SensorInfo(1, 10.0)))
    val currentPeople = CurrentPeopleUpdate(RoomID(serial = 777, name = "PancoPillo"), 777)
    
    val planimetric = AriadneMessage(
        Topology,
        Topology.Subtype.Planimetrics,
        Location.Admin >> Location.Master,
        plan
    )
    
    val ackTop = AriadneMessage(
        Topology,
        Topology.Subtype.Acknowledgement,
        Location.Cell >> Location.Master,
        cellInfo
    )
    
    val ack = AriadneMessage(
        Topology,
        Topology.Subtype.Acknowledgement,
        Location.Master >> Location.Admin,
        CellInfo.empty
    )
    
    val topologyViewedFromACell = AriadneMessage(
        Topology,
        ViewedFromACell,
        Location.Master >> Location.Cell,
        AreaViewedFromACell(plan)
    )
    
    val handshake = AriadneMessage(
        Handshake,
        Handshake.Subtype.CellToMaster,
        Location.Cell >> Location.Master,
        sensorsInfo
    )
    
    val currentPeopleUpdate = AriadneMessage(
        Update,
        Update.Subtype.CurrentPeople,
        Location.Cell >> Location.Master,
        currentPeople
    )
    
    val sensorsInfoUpdate = AriadneMessage(
        Update,
        Update.Subtype.Sensors,
        Location.Cell >> Location.Master,
        sensorsInfo
    )
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A TopologySupervisor" should {
        
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
    
        "At start time" must {
            "request the map to the system.admin" in {
    
                tester ! AriadneMessage(
                    Init,
                    Init.Subtype.Greetings,
                    Location.Cell >> Location.Self,
                    Greetings(List.empty)
                )
                
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
    
                probe.expectMsg(MasterSubscriber.TopologyLoadedACK)
                assert(probe.sender == tester.underlyingActor.subscriber)
    
                probe.expectMsg(ack)
                assert(probe.sender == tester.underlyingActor.admin)
            }
        }
        
        
        "When sociable" must {
            
            "wait for the Handshakes to be forwarded" in {
                tester ! handshake
    
                probe.expectMsg(handshake)
                assert(probe.sender == tester.underlyingActor.admin)
            }
    
            "once all the expected handshakes have been mapped, should notify the subscriber, " +
                "sending the Topology and becoming acknowledging" in {
                probe.expectMsg(MasterSubscriber.TopologyMappedACK)
                assert(probe.sender == tester.underlyingActor.subscriber)
                probe.expectMsg(topologyViewedFromACell)
                assert(probe.sender == tester.underlyingActor.publisher)
            }
        }
    
        "When Acknowledging " must {
        
            "Accept Topology Acknowledgement by single Cells" in {
                tester ! ackTop
            
            }
        
            "Accept Timeout Messages from it's watchdog Manager" in {
                // Internal Behaviour
            }
        
            "Accept final Acknowledgement from the WatchDogSupervisor and become proactive " in {
                //Internal Behaviour
            }
        }
        
        "When proactive" must {
            
            "accept new sensors values" in {
    
                val topology = mutable.HashMap(plan.rooms.map(r => r.cell.info.uri -> r): _*)
    
                val newCell = topology(sensorsInfo.cell.uri).cell
                    .copy(
                        info = sensorsInfo.cell,
                        sensors = sensorsInfo.sensors
                    )
    
                val newRoom = topology(sensorsInfo.cell.uri).copy(cell = newCell)
    
                topology.put(sensorsInfo.cell.uri, newRoom)
                tester ! sensorsInfoUpdate
                probe.expectMsg(
                    AriadneMessage(
                        Update, Update.Subtype.Admin, Location.Master >> Location.Admin,
                        AdminUpdate(0, topology.values.map(c => RoomDataUpdate(c)).toList)
                    )
                )
            }
            
            "accept new current people" in {
    
                val topology: mutable.Map[String, Room] =
                    mutable.HashMap(plan
                        .rooms.map(room => room.cell.info.uri -> room): _*)
    
                val oldRoom = topology(sensorsInfo.cell.uri)
    
                val newRoom: Room = topology(sensorsInfo.cell.uri)
                    .copy(
                        cell = Cell(sensorsInfo.cell, sensorsInfo.sensors),
                        currentPeople = currentPeople.currentPeople,
                        practicability = Practicability(
                            oldRoom.info.capacity,
                            currentPeople.currentPeople,
                            oldRoom.passages.length
                        )
                    )
    
                topology.put(sensorsInfo.cell.uri, newRoom)
                
                tester ! currentPeopleUpdate
    
                probe.expectMsg(
                    AriadneMessage(
                        Update, Update.Subtype.Admin, Location.Master >> Location.Admin,
                        AdminUpdate(0, topology.values.map(c => RoomDataUpdate(c)).toList)
                    )
                )
            }
    
            "accept late handshakes, temporary becoming acknowledging and then returning ProActive" in {
                tester ! handshake
                probe.expectMsg(topologyViewedFromACell)
                assert(probe.sender == tester.underlyingActor.publisher)
                tester ! ackTop
            }
    
            "resend mock handshakes to the system.admin when an unexpected planimetry is loaded" in {
    
                tester ! planimetric
    
                probe.expectMsg(ack)
                assert(probe.sender == tester.underlyingActor.admin)
        
                probe.expectMsg(handshake)
        
                assert(probe.sender == tester.underlyingActor.admin)
            }
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
    
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
                case msg => probe ! msg
            }
        }), self, NamingSystem.AdminSupervisor)
    
        val supervisor: TestActorRef[TopologySupervisor] =
            TestActorRef(Props[TopologySupervisor], self, NamingSystem.TopologySupervisor)
        
        override def receive: Receive = {
            case msg if sender == supervisor => probe forward msg
            case msg => supervisor forward msg
        }
    }
    
}
