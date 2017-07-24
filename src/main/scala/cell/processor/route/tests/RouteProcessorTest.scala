package cell.processor.route.tests

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import cell.processor.route.actors.RouteManager
import com.actors.CustomActor
import ontologies.messages.Location._
import ontologies.messages.MessageType.{Init, Route}
import ontologies.messages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class RouteProcessorTest extends TestKit(ActorSystem("RouteProcessorTest"))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
    
    val infoCells: Map[Int, InfoCell] = (1 to 9).map(i => i ->
        InfoCell(
            id = i,
            uri = "http://Arianna/Cell" + i + "@127.0.0.1:" + Random.nextInt(65535) + "/",
            name = "Cell" + i,
            roomVertices = Coordinates(Point(0, 0), Point(0, 6), Point(6, 0), Point(6, 6)),
            antennaPosition = Point(3, 3)
        )
    ).toMap
    
    val routeForAlarmSolution = List(infoCells(9), infoCells(6), infoCells(4), infoCells(5), infoCells(1), infoCells(2))
    
    val areaForAlarm = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            CellViewedFromACell(
                infoCells(1),
                neighbors = List(infoCells(2), infoCells(5)),
                passages = List(
                    Passage(infoCells(2).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(5).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                0.0
            ),
            CellViewedFromACell(
                infoCells(2),
                neighbors = List(infoCells(1), infoCells(3), infoCells(4)),
                passages = List(
                    Passage(infoCells(1).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(3).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(4).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                Random.nextInt(20),
                100
            ),
            CellViewedFromACell(
                infoCells(3),
                neighbors = List(infoCells(2), infoCells(6)),
                passages = List(
                    Passage(infoCells(2).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                150
            ),
            CellViewedFromACell(
                infoCells(4),
                neighbors = List(infoCells(2), infoCells(5), infoCells(6), infoCells(8)),
                passages = List(
                    Passage(infoCells(2).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(5).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(8).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                0.0
            ),
            CellViewedFromACell(
                infoCells(5),
                neighbors = List(infoCells(1), infoCells(4), infoCells(7)),
                passages = List(
                    Passage(infoCells(1).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(4).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(7).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                0.0
            ),
            CellViewedFromACell(
                infoCells(6),
                neighbors = List(infoCells(3), infoCells(4), infoCells(9)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(3).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                0.0
            ),
            CellViewedFromACell(
                infoCells(7),
                neighbors = List(infoCells(8), infoCells(5)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                100
            ),
            CellViewedFromACell(
                infoCells(8),
                neighbors = List(infoCells(4), infoCells(7), infoCells(9)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(7).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                Random.nextInt(20),
                Double.PositiveInfinity //10 * Random.nextDouble()
            ),
            CellViewedFromACell(
                infoCells(9),
                neighbors = List(infoCells(6), infoCells(8)),
                passages = List(
                    Passage(infoCells(6).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8).id, Point(3, 0), Point(3, 0))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                Random.nextInt(20),
                10
            )
        ))
    
    val routeForAreaSolution = List(infoCells(1), infoCells(5), infoCells(4), infoCells(6), infoCells(9))
    
    val areaForRoute = AreaViewedFromACell(
        Random.nextInt(Int.MaxValue),
        List(
            CellViewedFromACell(
                infoCells(1),
                neighbors = List(infoCells(2), infoCells(5)),
                passages = List(
                    Passage(infoCells(2).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(5).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 0.0
            ),
            CellViewedFromACell(
                infoCells(2),
                neighbors = List(infoCells(1), infoCells(3), infoCells(4)),
                passages = List(
                    Passage(infoCells(1).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(3).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(4).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                capacity = Random.nextInt(20),
                practicability = 100
            ),
            CellViewedFromACell(
                infoCells(3),
                neighbors = List(infoCells(2), infoCells(6)),
                passages = List(
                    Passage(infoCells(2).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 100
            ),
            CellViewedFromACell(
                infoCells(4),
                neighbors = List(infoCells(2), infoCells(5), infoCells(6), infoCells(8)),
                passages = List(
                    Passage(infoCells(2).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(5).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(6).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(8).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 0.0
            ),
            CellViewedFromACell(
                infoCells(5),
                neighbors = List(infoCells(1), infoCells(4), infoCells(7)),
                passages = List(
                    Passage(infoCells(1).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(4).id, Point(3, 6), Point(3, 6)),
                    Passage(infoCells(7).id, Point(6, 3), Point(6, 3))
                ),
                isEntryPoint = false,
                isExitPoint = true,
                capacity = Random.nextInt(20),
                practicability = 0.0
            ),
            CellViewedFromACell(
                infoCells(6),
                neighbors = List(infoCells(3), infoCells(4), infoCells(9)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(3).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = true,
                capacity = Random.nextInt(20),
                practicability = 0.0 //Double.PositiveInfinity//
            ),
            CellViewedFromACell(
                infoCells(7),
                neighbors = List(infoCells(8), infoCells(5)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 100
            ),
            CellViewedFromACell(
                infoCells(8),
                neighbors = List(infoCells(4), infoCells(7), infoCells(9)),
                passages = List(
                    Passage(infoCells(4).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(7).id, Point(3, 0), Point(3, 0)),
                    Passage(infoCells(9).id, Point(3, 6), Point(3, 6))
                ),
                isEntryPoint = true,
                isExitPoint = true,
                capacity = Random.nextInt(20),
                practicability = 100
            ),
            CellViewedFromACell(
                infoCells(9),
                neighbors = List(infoCells(6), infoCells(8)),
                passages = List(
                    Passage(infoCells(6).id, Point(0, 3), Point(0, 3)),
                    Passage(infoCells(8).id, Point(3, 0), Point(3, 0))
                ),
                isEntryPoint = false,
                isExitPoint = false,
                capacity = Random.nextInt(20),
                practicability = 10
            )
        ))
    
    override def afterAll {
        TestKit.shutdownActorSystem(system)
    }
    
    "A RouteProcessor Actor" must {
        
        "Respond with RouteResponse, holding the SHP from the specified source to the specified target" in {
            
            val probe = TestProbe()
            
            val core = system.actorOf(Props(new AssertActor(probe)), "AssertActors")
            
            core ! AriadneMessage(
                Route, Route.Subtype.Info,
                Location.User >> Location.Cell,
                RouteInfo(
                    RouteRequest("1234", fromCell = infoCells(1), toCell = infoCells(9), isEscape = false),
                    areaForRoute
                )
            )
            
            probe.expectMsg(
                FiniteDuration(10L, duration.SECONDS),
                AriadneMessage(
                    Route, Route.Subtype.Response,
                    Location.Cell >> Location.User,
                    RouteResponse(
                        RouteRequest("1234", infoCells(1), infoCells(9), isEscape = false),
                        routeForAreaSolution
                    )
                ))
        }
    }
    
    private class AssertActor(probe: TestProbe) extends CustomActor {
        
        val routeManager: ActorRef = context.actorOf(Props[RouteManager], "RouteManager")
        
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
                probe.ref forward msg
        }
    }
    
}
