package processor.route.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.actors.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Init, Route}
import ontologies.messages._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import system.names.NamingSystem

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class RouteProcessorTest extends TestKit(ActorSystem("RouteProcessorTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    val infoCells: Map[Int, (CellInfo, RoomInfo)] = (1 to 9).map(i => i ->
        (
            CellInfo(uri = "http://Arianna/Cell" + i + "@127.0.0.1:" + Random.nextInt(65535) + "/", port = 0),
            RoomInfo(
                id = RoomID(i, "Cell" + i),
                isEntryPoint = if (i == 2 || i == 8) true else false,
                isExitPoint = if (i == 2 || i == 8) true else false,
                capacity = Random.nextInt(20),
                roomVertices = Coordinates(Point(0, 0), Point(0, 6), Point(6, 0), Point(6, 6)),
                antennaPosition = Point(3, 3),
                squareMeters = Random.nextInt(100)
            )
        )
    ).toMap
    
    val routeForAlarmSolution = List(infoCells(9), infoCells(6), infoCells(4), infoCells(5), infoCells(1), infoCells(2))
    
    val areaForAlarm = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            RoomViewedFromACell(
                infoCells(1)._2,
                infoCells(1)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(5)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(5)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(2)._2,
                infoCells(2)._1,
                neighbors = List(infoCells(1)._2.id, infoCells(3)._2.id, infoCells(4)._2.id),
                passages = List(
                    Passage(infoCells(1)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(3)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(4)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(3)._2,
                infoCells(3)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(6)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 150
            ),
            RoomViewedFromACell(
                infoCells(4)._2,
                infoCells(4)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(5)._2.id, infoCells(6)._2.id, infoCells(8)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(5)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(8)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(5) _2,
                infoCells(5) _1,
                neighbors = List(infoCells(1)._2.id, infoCells(4)._2.id, infoCells(7)._2.id),
                passages = List(
                    Passage(infoCells(1)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(4)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(7)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(6) _2,
                infoCells(6) _1,
                neighbors = List(infoCells(3)._2.id, infoCells(4)._2.id, infoCells(9)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(3)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(7) _2,
                infoCells(7) _1,
                neighbors = List(infoCells(8)._2.id, infoCells(5)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(8) _2,
                infoCells(8) _1,
                neighbors = List(infoCells(4)._2.id, infoCells(7)._2.id, infoCells(9)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(7)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = Double.PositiveInfinity //10 * Random.nextDouble()
            ),
            RoomViewedFromACell(
                infoCells(9) _2,
                infoCells(9) _1,
                neighbors = List(infoCells(6)._2.id, infoCells(8)._2.id),
                passages = List(
                    Passage(infoCells(6)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8)._2.id.serial, Point(3, 0), Point(3, 0))
                ),
                practicability = 10
            )
        ))
    
    val routeForAreaSolution = List(infoCells(1), infoCells(5), infoCells(4), infoCells(6), infoCells(9))
    
    val areaForRoute = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            RoomViewedFromACell(
                infoCells(1)._2,
                infoCells(1)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(5)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(5)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(2)._2,
                infoCells(2)._1,
                neighbors = List(infoCells(1)._2.id, infoCells(3)._2.id, infoCells(4)._2.id),
                passages = List(
                    Passage(infoCells(1)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(3)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(4)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(3)._2,
                infoCells(3)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(6)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 150
            ),
            RoomViewedFromACell(
                infoCells(4)._2,
                infoCells(4)._1,
                neighbors = List(infoCells(2)._2.id, infoCells(5)._2.id, infoCells(6)._2.id, infoCells(8)._2.id),
                passages = List(
                    Passage(infoCells(2)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(5)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(8)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(5) _2,
                infoCells(5) _1,
                neighbors = List(infoCells(1)._2.id, infoCells(4)._2.id, infoCells(7)._2.id),
                passages = List(
                    Passage(infoCells(1)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(4)._2.id.serial, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(7)._2.id.serial, Point(6, 3), Point(6, 3))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(6) _2,
                infoCells(6) _1,
                neighbors = List(infoCells(3)._2.id, infoCells(4)._2.id, infoCells(9)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(3)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 0.0
            ),
            RoomViewedFromACell(
                infoCells(7) _2,
                infoCells(7) _1,
                neighbors = List(infoCells(8)._2.id, infoCells(5)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(8) _2,
                infoCells(8) _1,
                neighbors = List(infoCells(4)._2.id, infoCells(7)._2.id, infoCells(9)._2.id),
                passages = List(
                    Passage(infoCells(4)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(7)._2.id.serial, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9)._2.id.serial, Point(3, 6), Point(3, 6))
                ),
                practicability = 100
            ),
            RoomViewedFromACell(
                infoCells(9) _2,
                infoCells(9) _1,
                neighbors = List(infoCells(6)._2.id, infoCells(8)._2.id),
                passages = List(
                    Passage(infoCells(6)._2.id.serial, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8)._2.id.serial, Point(3, 0), Point(3, 0))
                ),
                practicability = 10
            )
        )
    )
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A RouteProcessor Actor" must {
    
        val probe = TestProbe()
    
        val core: TestActorRef[Tester] = TestActorRef(Props(new Tester(probe.ref)), "Tester")
    
        "Answer with a RouteResponse holding the SHP from the specified source to the specified target" in {
            core ! AriadneMessage(
                Route, Route.Subtype.Info,
                Location.User >> Location.Cell,
                RouteInfo(
                    RouteRequest("15469",
                        fromCell = infoCells(1)._2.id,
                        toCell = infoCells(9)._2.id,
                        isEscape = false
                    ),
                    areaForRoute
                )
            )
        
            probe.expectMsg(
                FiniteDuration(10L, duration.SECONDS),
                AriadneMessage(
                    Route, Route.Subtype.Response,
                    Location.Cell >> Location.User,
                    RouteResponse(
                        RouteRequest("15469",
                            fromCell = infoCells(1)._2.id,
                            toCell = infoCells(9)._2.id,
                            isEscape = false
                        ),
                        routeForAreaSolution.map(p => p._2.id)
                    )
                ))
        }
    
        "Answer with a RouteResponse holding the SHP to the exit when an Escape Request is received" in {
            core ! AriadneMessage(
                Route, Route.Subtype.Info,
                Location.User >> Location.Cell,
                RouteInfo(
                    RouteRequest("964512",
                        fromCell = infoCells(9)._2.id,
                        toCell = RoomID.empty,
                        isEscape = true
                    ),
                    areaForAlarm
                )
            )
        
            probe.expectMsg(
                FiniteDuration(10L, duration.SECONDS),
                AriadneMessage(
                    Route, Route.Subtype.Response,
                    Location.Cell >> Location.User,
                    RouteResponse(
                        RouteRequest("964512",
                            fromCell = infoCells(9)._2.id,
                            toCell = infoCells(2)._2.id,
                            isEscape = true
                        ),
                        routeForAlarmSolution.map(p => p._2.id)
                    )
                ))
        }
    }
    
    private class Tester(probe: ActorRef) extends CustomActor {
    
        val routeManager: ActorRef = context.actorOf(Props[RouteManager], NamingSystem.RouteManager)
        
        override def preStart {
            
            routeManager ! AriadneMessage(
                Init,
                Init.Subtype.Greetings,
                Location.Cell >> Location.Self,
                Greetings(List.empty)
            )
        }
        
        override def receive: Receive = {
            case msg@AriadneMessage(Route, Route.Subtype.Info, _, _) => routeManager ! msg
            case msg@AriadneMessage(Route, Route.Subtype.Response, _, cnt: RouteResponse) =>
                log.info(cnt.route.map(i => i.name).mkString(" -> "))
                probe forward msg
        }
    }
}
