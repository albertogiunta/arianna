package processor.tests

import processor.algorithms.AStarSearch
import processor.algorithms.Dijkstra.Graph

/**
  * Created by Alessandro on 12/07/2017.
  */
object TestAStarSearch extends App {
    
    //    val tree: Graph[List[Boolean]] = Dijkstra.tree(14)
    
    val tree: Graph[String] = Map(
        "A" -> Map("B" -> 1.0, "C" -> 1.0, "D" -> 1.0),
        "B" -> Map("C" -> 1.0, "D" -> 1.0),
        "C" -> Map(),
        "D" -> Map("A" -> 1.0, "E" -> 1.0),
        "E" -> Map("Z" -> 1.0),
        "Z" -> Map()
    )
    
    //println(tree)
    val time = System.currentTimeMillis
    //    println(AStarSearch.A_*(tree)(List(true), Nil)(AStarSearch.Extractor.toList))
    
    println(AStarSearch.A_*(tree)("A", "Z")(AStarSearch.Extractor.toList))
    
    println("Execution Time : " + (System.currentTimeMillis() - time) / 1000.0)
}
