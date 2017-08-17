package cell.processor.route.algorithms

import cell.processor.route.algorithms.AStarSearch.Graph
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
  * Created by Alessandro on 12/07/2017.
  */
@RunWith(classOf[JUnitRunner])
class AStarSearchTest extends FunSuite with BeforeAndAfter {
    
    val graph: Graph[String] = Map(
        "A" -> Map("B" -> 1.0, "C" -> 1.0, "D" -> 1.0, "Z" -> 4.0),
        "B" -> Map("C" -> 1.0, "D" -> 1.0),
        "C" -> Map(),
        "D" -> Map("A" -> 1.0, "E" -> 1.0),
        "E" -> Map("Z" -> 1.0),
        "Z" -> Map()
    )
    
    val solutionAsList: (List[String], Double) = (List("A", "D", "E", "Z"), 3.0)
    val solutionAsMap: (Map[String, String], Double) = (Map("A" -> "D", "D" -> "E", "E" -> "Z"), 3.0)
    
    test("The shortest path as List") {
        assert(AStarSearch.A_*(graph)("A", "Z")(AStarSearch.Extractors.toList) == solutionAsList)
    }
    
    test("The shortest path as Map") {
        assert(AStarSearch.A_*(graph)("A", "Z")(AStarSearch.Extractors.toMap) == solutionAsMap)
    }
    
    test("Test shortest past from self to self") {
        assert(AStarSearch.A_*(graph)("A", "A")(AStarSearch.Extractors.toList) == (List("A"), 0.0))
    }
}
