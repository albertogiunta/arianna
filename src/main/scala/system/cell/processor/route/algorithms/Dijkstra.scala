package system.cell.processor.route.algorithms

import system.cell.processor.route.algorithms.Dijkstra.Graph

import scala.annotation.tailrec
import scala.collection.immutable.HashMap

/**
  * Thanks to Michael Ummels@{http://www.ummels.de/2015/01/18/dijkstra-in-scala/}
  *
  * Created by Alessandro on 12/07/2017.
  */
object Dijkstra {
    
    type Graph[N] = N => Map[N, Double]
    
    case class Node[N](name: N, neighbors: Map[N, Double])
    
    def shortestPath[N](graph: Graph[N])(source: N, target: N): Option[List[N]] = {
        val pred = dijkstra(graph)(source)._2
        if (pred.contains(target) || source == target) Some(iterateRight(target)(pred.get))
        else None
    }
    
    def dijkstra[N](g: Graph[N])(source: N): (Map[N, Double], Map[N, N]) = {
        
        @tailrec def go(active: Set[N], res: Map[N, Double], pred: Map[N, N]): (Map[N, Double], Map[N, N]) =
            if (active.isEmpty) (res, pred)
            else {
                val node = active.minBy(res)
                val cost = res(node)
                val neighbours = for {
                    (n, c) <- g(node) if cost + c < res.getOrElse(n, Double.MaxValue)
                } yield n -> (cost + c)
                go(active - node ++ neighbours.keys,
                    res ++ neighbours,
                    pred ++ neighbours mapValues (_ => node))
            }
        
        go(Set(source), Map(source -> 0), Map.empty)
    }
    
    private[this] def iterateRight[N](x: N)(f: N => Option[N]): List[N] = {
        def go(x: N, acc: List[N]): List[N] = f(x) match {
            case None => x :: acc
            case Some(y) => go(y, x :: acc)
        }
        
        go(x, List.empty)
    }
    
    def tree(depth: Int): Graph[List[Boolean]] = {
        case x if x.length < depth =>
            HashMap((true :: x) -> 1, (false :: x) -> 2)
        case x if x.length == depth => HashMap(Nil -> 1)
        case _ => HashMap.empty
    }
}

object DijkstraTest extends App {
    
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
}