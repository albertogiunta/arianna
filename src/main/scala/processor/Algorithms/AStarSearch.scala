package processor.Algorithms

import processor.Algorithms.Dijkstra.Graph

import scala.annotation.tailrec
import scala.collection.mutable

/**
  * A simple implementation of the A* Search Heuristic Algorithm
  * Created by Alessandro on 12/07/2017.
  */
object AStarSearch {
    
    def A_*[N](graph: Graph[N])(source: N, target: N): Map[N, N] = {
        
        // The set of nodes already evaluated
        val closedSet = mutable.HashSet.empty[N]
        
        // The set of currently discovered nodes that are not evaluated yet.
        // Initially, only the start node is known.
        val fringe = mutable.HashSet(source)
        
        // For each node, which node it can most efficiently be reached from.
        // If a node can be reached from many nodes, routes will eventually contain the
        // most efficient previous step.
        val routes = mutable.HashMap.empty[N, N]
        
        // For each node, the cost of getting from the start node to that node.
        // In this version instead of adding nodes with Infinity Weight,
        // They just are absent, thus need to be added when computed.
        // The cost of going from start to start is zero.
        val gScores = mutable.HashMap(source -> 0.0)
        
        // For each node, the total cost of getting from the start node to the goal
        // by passing by that node. That value is partly known, partly heuristic.
        // For the first node, that value is completely heuristic.
        val fScores = mutable.HashMap(source -> heuristicCost(graph)(source, target))
        
        while (fringe.nonEmpty) {
            val current: N = fScores.filter(e => !closedSet(e._1)).minBy(e => e._2)._1
            
            if (current == target) return reconstructPath[N](routes, source, current)
            else {
                fringe -= current
                closedSet += current
                
                graph(current).filter(neighbor => !closedSet(neighbor._1))
                    .foreach(neighbor => {
                        fringe += neighbor._1
                        
                        val gScore = gScores(current) + neighbor._2
                        
                        if (!gScores.contains(neighbor._1) || gScore <= gScores(neighbor._1)) {
                            routes.put(neighbor._1, current)
                            gScores.put(neighbor._1, gScore)
                            fScores.put(neighbor._1, gScore + heuristicCost(graph)(neighbor._1, target))
                        }
                    })
            }
        }
        
        throw new Exception("No viable Route from Source " + source + " to Target " + target + "...")
    }
    
    def heuristicCost[N](g: Graph[N])(source: N, target: N): Double = 0.0
    
    def reconstructPath[N](routes: mutable.HashMap[N, N], source: N, target: N): Map[N, N] = {
        
        @tailrec def go(sp: Map[N, N], current: N, successor: N): Map[N, N] = {
            println(sp)
            if (current == source) sp.updated(source, successor)
            else go(sp.updated(current, successor), routes(current), current)
        }
        
        go(Map.empty, routes(target), target)
    }
}
