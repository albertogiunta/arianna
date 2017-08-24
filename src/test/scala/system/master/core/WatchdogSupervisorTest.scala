package system.master.core

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import com.utils.Watchdog
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem
import system.ontologies.messages.Location.PreMade.selfToSelf
import system.ontologies.messages.MessageType.Topology.Subtype.Acknowledgement
import system.ontologies.messages.MessageType.{Init, Topology}
import system.ontologies.messages.{AriadneMessage, CellInfo, Greetings, Location}

class WatchdogSupervisorTest extends TestKit(ActorSystem("WatchdogSupervisorTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    "A WatchdogSupervisor" should {
        
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
        
        "when receptive " must {
            
            "Accept new cells to hook up" in {
                tester ! CellInfo.empty
                probe.expectNoMsg()
            }
            
            "Accept timer start up command" in {
                tester ! true
                probe.expectNoMsg()
            }
        }
        
        "when acknowledging " must {
            
            "Accept time-out signals" in {
                
                probe.expectMsg(Watchdog.WatchDogNotification(self))
                
                assert(probe.sender == tester.underlyingActor.watchdog)
            }
            
            "Accept new ACKs" in {
                tester ! AriadneMessage(Topology, Acknowledgement, Location.PreMade.cellToMaster, CellInfo.empty)
                
                probe.expectMsg(Watchdog.WatchDogNotification(true))
                
                assert(probe.sender == tester.underlyingActor.watchdog)
                
                probe.expectNoMsg()
            }
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
        
        val watchdog: TestActorRef[WatchdogSupervisor] =
            TestActorRef(Props[WatchdogSupervisor], self, NamingSystem.WatchdogSupervisor)
        
        override def preStart: Unit = {
            watchdog ! AriadneMessage(
                Init, Init.Subtype.Greetings,
                selfToSelf, Greetings(List())
            )
        }
        
        override def receive: Receive = {
            case msg if sender == watchdog => probe forward msg
            case msg => watchdog forward msg
        }
    }
    
}
