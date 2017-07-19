package cell.processor.tests

import cell.processor.route.algorithms.Dijkstra.Graph
import cell.processor.route.algorithms.{AStarSearch, Dijkstra}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Alessandro on 12/07/2017.
  */
object TestAStarSearch extends App {
    
    val tree1: Graph[List[Boolean]] = Dijkstra.tree(15)
    
    //    val tree2: Graph[String] = Map(
    //        "A" -> Map("B" -> 1.0, "C" -> 1.0, "D" -> 1.0),
    //        "B" -> Map("C" -> 1.0, "D" -> 1.0),
    //        "C" -> Map(),
    //        "D" -> Map("A" -> 1.0, "E" -> 1.0),
    //        "E" -> Map("Z" -> 1.0),
    //        "Z" -> Map()
    //    )
    //
    //    val time2 = System.currentTimeMillis
    //
    //    println(AStarSearch.A_*(tree2)("A", "Z")(AStarSearch.Extractors.toList))
    //
    //    println("Execution Time : " + (System.currentTimeMillis() - time2) / 1000.0)
    
    val futures = (0 to 3).map(_ => Future {
        AStarSearch.A_*(tree1)(List(true), Nil)(AStarSearch.Extractors.toList)
    })
    
    val toClose = Future {
        futures.map(f => Await.result(f, Duration.Inf)).minBy(res => res._2)._1
    }
    
    toClose.onComplete(p => if (p.isSuccess) {
        println(p.get)
    })
    
    println("Ciaone")
    
    while (!toClose.isCompleted) {
        // Wait
    }
    
    Thread.sleep(1000)
}
