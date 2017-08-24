package com.utils

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.actors.CustomActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

/**
  * Created by Matteo Gabellini on 15/08/2017.
  */
class BasicWatchdogTest extends TestKit(ActorSystem("BasicWatchDogTest"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A Watch Dog" should {
        val proxy = TestProbe()
        val parent = system.actorOf(Props(new TestParent(proxy.ref)), "TestParent")
        
        "send a notification when its wait time is exceeded" in {
            proxy.send(parent, "start watch dog")
            proxy.expectMsgClass(
                (Watchdog.waitTime + Watchdog.waitTime / 2) millisecond,
                Watchdog.WatchDogNotification.getClass
            )
        }
        
        "do not send more notification when the expected event occurred" in {
            proxy.send(parent, "start watch dog")
            proxy.expectNoMsg(Watchdog.waitTime / 2 millisecond)
            proxy.send(parent, "stop watch dog")
            proxy.expectNoMsg(Watchdog.waitTime millisecond)
        }
    }
    
}

class TestParent(proxy: ActorRef) extends CustomActor {
    
    var watchDog: Watchdog = _
    
    override def receive: Receive = {
        case "start watch dog" =>
            watchDog = new BasicWatchdog(self)
            watchDog.asInstanceOf[BasicWatchdog].start()
        case "stop watch dog" => watchDog.notifyEventOccurred
        case msg@Watchdog.WatchDogNotification =>
            proxy ! msg
    }
}