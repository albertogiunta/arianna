package system.cell.processor.route.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem
import system.ontologies.messages._

class RouteCachingTest extends TestKit(ActorSystem("RouteProcessorTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    val req = RouteRequest("asdfgh", RoomID(0, "A"), RoomID(1, "D"), isEscape = false)
    
    val rInfo = RouteInfo(req, AreaViewedFromACell(0, List.empty))
    
    val res = RouteResponse(req, List(RoomID(0, "A"), RoomID(1, "B"), RoomID(2, "C"), RoomID(3, "D")))
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A route cacher" should {
        val probe = TestProbe()
        val routeManager: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
        
        "Save and returns new computed routes while timeToLive is still valid" in {
            
            routeManager ! res
            
            routeManager ! rInfo
            
            probe.expectMsg(res)
            
            assert(probe.sender == routeManager.underlyingActor.routeCache)
        }
        
        "Holding the route for the given time in Milliseconds (5000)" in {
            
            routeManager ! res
            
            routeManager ! rInfo
            
            probe.expectMsg(res)
            
            assert(probe.sender == routeManager.underlyingActor.routeCache)
            
            Thread.sleep(5000L)
            
            routeManager ! rInfo
            
            probe.expectMsg(rInfo)
        }
    }
    
    
    private class Tester(probe: ActorRef) extends CustomActor {
        
        val manager = system.actorOf(Props(new CustomActor {
            
            override def receive: Receive = {
                case msg => probe forward msg
            }
        }), NamingSystem.RouteManager)
        
        val routeCache: ActorRef = context.actorOf(Props(new CacheManager(5000L)), NamingSystem.CacheManager)
        
        override def receive: Receive = {
            case msg: MessageContent if sender != routeCache =>
                routeCache ! msg
            case msg => probe forward msg
            
        }
    }
    
}

object RouteCachingTest {

}