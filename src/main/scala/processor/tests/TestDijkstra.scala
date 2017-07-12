package processor.tests

import processor.algorithms.Dijkstra
import processor.algorithms.Dijkstra.Graph

/**
  * Created by Alessandro on 12/07/2017.
  */
object TestDijkstra extends App {
    val tree = Dijkstra.tree(12)
    
    val tree2: Graph[String] = Map(
        "A" -> Map("B" -> 1.0, "C" -> 1.0, "D" -> 1.0),
        "B" -> Map("C" -> 1.0, "D" -> 1.0),
        "C" -> Map(),
        "D" -> Map("A" -> 1.0, "E" -> 1.0),
        "E" -> Map("Z" -> 1.0),
        "Z" -> Map()
    )
    
    println(tree2)
    val time = System.currentTimeMillis
    Dijkstra.dijkstra(tree2)("A")
    println("Execution Time : " + (System.currentTimeMillis() - time) / 1000.0)
    
    println(tree)
    val time1 = System.currentTimeMillis
    Dijkstra.dijkstra(tree)(List(true)) //._1 foreach println
    
    println("Execution Time : " + (System.currentTimeMillis() - time1) / 1000.0)
    
    val time2 = System.currentTimeMillis
    Dijkstra.dijkstraWithPriority(tree)(List(true)) //._1 foreach println
    
    println("Execution Time : " + (System.currentTimeMillis() - time2) / 1000.0)
}
