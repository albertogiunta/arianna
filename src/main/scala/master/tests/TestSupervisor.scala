package master.tests

/**
  * Created by Xander_C on 09/07/2017.
  */
object TestSupervisor extends App {
    
    def calculatePracticability(capacity: Double, load: Double, flows: Double): Double = {
        val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
        
        (load * 1.05) / capacity * (100.0 / log_b(4.0, flows))
    }
    
    println(calculatePracticability(50, 50, 2))
}
