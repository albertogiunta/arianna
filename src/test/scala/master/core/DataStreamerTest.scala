package master.core

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Init, Update}
import ontologies.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by Xander_C on 09/07/2017.
  */
class DataStreamerTest extends TestKit(ActorSystem("DataStreamerTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    "A DataStreamer " should {
        
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
        
        tester ! AriadneMessage(
            Init,
            Init.Subtype.Greetings,
            Location.Cell >> Location.Self,
            Greetings(List.empty)
        )
        
        "Submit incoming collection to the Hot Stream" in {
            
            tester ! List.empty[Cell]
            
            probe.expectMsg("I'm the hot stream")
        }
        
        "The submitted data should be delivered to the Admin Manager" in {
            
            probe.expectMsg(AriadneMessage(
                Update,
                Update.Subtype.UpdateForAdmin,
                Location.Master >> Location.User,
                UpdateForAdmin(List.empty[Cell].map(c => CellDataUpdate(c)))
            ))
            
            assert(probe.sender == tester.underlyingActor.admin)
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
    
        val streamer: TestActorRef[TopologySupervisor] =
            TestActorRef(Props(new DataStreamer((msg, target) => {
                probe ! "I'm the hot stream"
                target ! msg
            })), self, "DataStreamer")
    
        val admin: TestActorRef[CustomActor] = TestActorRef(Props(new CustomActor {
            override def receive: Receive = {
                case msg => probe ! msg
            }
        }), self, "AdminManager")
        
        override def receive: Receive = {
            case msg if sender == streamer => probe forward msg
            case msg => streamer forward msg
        }
    }
    
}

object Run extends App {
    
    val system = ActorSystem("DataStreamerTest")
    
    val streamer: ActorRef =
        system.actorOf(Props(new DataStreamer()), "DataStreamer")
    
    val admin: ActorRef = system.actorOf(Props(new CustomActor {
        override def receive: Receive = {
            case msg => log.info(msg.toString)
        }
    }), "AdminManager")
    
    
    streamer ! AriadneMessage(
        Init,
        Init.Subtype.Greetings,
        Location.Cell >> Location.Self,
        Greetings(List.empty)
    )
    
    streamer ! List.empty[Cell]
    
}