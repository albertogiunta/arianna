package system.master.core

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem
import system.ontologies.messages.Location._
import system.ontologies.messages.MessageType.Update
import system.ontologies.messages._

/**
  * Created by Xander_C on 09/07/2017.
  */
class DataStreamerTest extends TestKit(ActorSystem("DataStreamerTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A DataStreamer " should {
        
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
        
        "Submit incoming collection to the Hot Stream" in {
    
            tester ! List.empty[Room]
            
            probe.expectMsg("I'm the hot stream")
        }
        
        "The submitted data should be delivered to the Admin Manager" in {
            
            probe.expectMsg(AriadneMessage(
                Update,
                Update.Subtype.Admin,
                Location.Master >> Location.User,
                AdminUpdate(0, List.empty[Room].map(c => RoomDataUpdate(c)))
            ))
            
            assert(probe.sender == tester.underlyingActor.admin)
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
    
        val admin: TestActorRef[CustomActor] = TestActorRef(Props(new CustomActor {
            override def receive: Receive = {
                case msg => probe ! msg
            }
        }), self, NamingSystem.AdminSupervisor)
    
        val streamer: TestActorRef[TopologySupervisor] =
            TestActorRef(Props(new DataStreamer(
                target = child(NamingSystem.AdminSupervisor).get,
                (msg, dest) => {
                    probe ! "I'm the hot stream"
                    dest ! msg
                })
            ), self, NamingSystem.DataStreamer)
        
        override def receive: Receive = {
            case msg if sender == streamer => probe forward msg
            case msg => streamer forward msg
        }
    }
}