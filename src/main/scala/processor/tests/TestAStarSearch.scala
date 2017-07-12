package processor.tests

import processor.Algorithms.AStarSearch
import processor.Algorithms.Dijkstra.Graph

/**
  * Created by Alessandro on 12/07/2017.
  */
object TestAStarSearch extends App {
    
    //    val tree : Graph[List[Boolean]] = Dijkstra.tree(15)
    
    val tree: Graph[String] = Map(
        "A" -> Map("B" -> 1.0, "C" -> 1.0, "D" -> 1.0),
        "B" -> Map("C" -> 1.0, "D" -> 1.0),
        "C" -> Map(),
        "D" -> Map("A" -> 1.0, "E" -> 1.0),
        "E" -> Map("Z" -> 1.0),
        "Z" -> Map()
    )
    
    println(tree)
    val time1 = System.currentTimeMillis
    //    println(AStarSearch.A_*(tree)(List(true), Nil))//._1 foreach println
    println(AStarSearch.A_*(tree)("A", "Z")) //._1 foreach println
    
    println("Execution Time : " + (System.currentTimeMillis() - time1) / 1000.0)
    
    
}
