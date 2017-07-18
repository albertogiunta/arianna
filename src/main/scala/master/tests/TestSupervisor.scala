package master.tests

/**
  * Created by Xander_C on 09/07/2017.
  */
object TestSupervisor extends App {
    
    def calculatePracticability(capacity: Double, load: Double, flows: Double): Double = {
        val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
    
        1 / (load * 1.05 / capacity * (if (flows == 1) 0.25 else if (flows > 4.0) log_b(3.0, 4.25) else log_b(3.0, flows)))
    }
    
    println(calculatePracticability(100, 1000, 3))
}
