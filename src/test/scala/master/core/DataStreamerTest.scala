package master.core

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Update
import ontologies.messages._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem

/**
  * Created by Xander_C on 09/07/2017.
  */
@RunWith(classOf[JUnitRunner])
class DataStreamerTest extends TestKit(ActorSystem("DataStreamerTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
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
        }), self, NamingSystem.AdminManager)
    
        val streamer: TestActorRef[TopologySupervisor] =
            TestActorRef(Props(new DataStreamer(
                target = child(NamingSystem.AdminManager).get,
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