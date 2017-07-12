package processor.algorithms

import scala.annotation.tailrec
import scala.collection.mutable

/**
  * A simple implementation of the A* Search Heuristic Algorithm
  * Created by Alessandro on 12/07/2017.
  */
object AStarSearch {
    
    type Graph[N] = N => Map[N, Double]
    
    def A_*[N, R](graph: Graph[N])(source: N, target: N)(extractor: (Map[N, N], N, N) => R): R = {
        
        def heuristicCost(g: Graph[N])(source: N, target: N): Double = 0.0
        
        // The set of nodes already evaluated
        val closed: mutable.Set[N] = mutable.HashSet.empty[N]
        
        // The set of currently discovered nodes that are not evaluated yet.
        // Initially, only the start node is known.
        val fringe: mutable.Set[N] = mutable.HashSet(source)
        
        // For each node, which node it can most efficiently be reached from.
        // If a node can be reached from many nodes, routes will eventually contain the
        // most efficient previous step.
        val routes: mutable.Map[N, N] = mutable.HashMap.empty[N, N]
        
        // For each node, the cost of getting from the start node to that node.
        // In this version instead of adding nodes with Infinity Weight,
        // They just are absent, thus need to be added when computed.
        // The cost of going from start to start is zero.
        val gScores: mutable.Map[N, Double] = mutable.HashMap(source -> 0.0)
        
        // For each node, the total cost of getting from the start node to the goal
        // by passing by that node. That value is partly known, partly heuristic.
        // For the first node, that value is completely heuristic.
        val fScores: mutable.Map[N, Double] = mutable.HashMap(source -> heuristicCost(graph)(source, target))
        
        @tailrec def go(current: N): R =
            if (fringe.isEmpty) throw new Exception()
            else if (current == target) extractor(routes.toMap, source, target)
            else {
                fringe -= current
                closed += current
    
                graph(current).filter(neighbor => !closed(neighbor._1))
                    .foreach(neighbor => {
                        fringe += neighbor._1
                        
                        val gScore = gScores(current) + neighbor._2
                        
                        if (!gScores.contains(neighbor._1) || gScore <= gScores(neighbor._1)) {
                            routes.put(neighbor._1, current)
                            gScores.put(neighbor._1, gScore)
                            fScores.put(neighbor._1, gScore + heuristicCost(graph)(neighbor._1, target))
                        }
                    })
    
                // Need to find a way to improve this
                go(fScores.filter(e => !closed(e._1)).minBy(e => e._2)._1)
            }
        
        go(source)
        
    }
    
    object Extractor {
        def toMap[N](routes: Map[N, N], source: N, target: N): Map[N, N] = {
            
            @tailrec def go(sp: Map[N, N], current: N, successor: N): Map[N, N] = {
                
                if (current == source) sp.updated(source, successor)
                else go(sp.updated(current, successor), routes(current), current)
            }
            
            go(Map.empty, routes(target), target)
        }
        
        def toList[N](routes: Map[N, N], source: N, target: N): List[N] = {
            
            @tailrec def go(sp: List[N], current: N): List[N] = {
                
                if (current == source) source :: sp
                else go(current :: sp, routes(current))
            }
            
            go(List(target), routes(target))
        }
    }
    
}
