package scala.admin.actors

import admin.actors.ChartManager
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.Interface
import ontologies.messages._
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.junit.JUnitRunner
import system.names.NamingSystem


@RunWith(classOf[JUnitRunner])
class ChartManagerTest extends TestKit(ActorSystem("InterfaceManagerTest")) with WordSpecLike {

    "A ChartManager" must {
        val probe = TestProbe()
        val tester: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")

        val closeChart = AriadneMessage(Interface, Interface.Subtype.CloseChart, Location.Admin >> Location.Self, Empty())

        "Let his parent know that the chart has been closed" in {
            tester ! closeChart
            probe.expectMsg(closeChart)
        }
    }

    private class Tester(probe: ActorRef) extends CustomActor {

        val interfaceManager: TestActorRef[CustomActor] =
            TestActorRef(Props(new CustomActor {
                val chartManager = context.actorOf(Props[ChartManager], NamingSystem.ChartManager)

                override def receive: Receive = {
                    case msg if sender == chartManager => {
                        probe ! msg
                    }
                    case msg => chartManager forward msg
                }
            }), self, NamingSystem.InterfaceManager)

        override def receive: Receive = {
            //case msg if sender == adminActor => probe forward msg
            case msg => interfaceManager forward msg
        }
    }

}
