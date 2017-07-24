package common.utils

object Practicability {
    
    def apply(capacity: Int, load: Int, flows: Int): Double = {
        val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
        1 / (load * 1.05 / capacity * (if (flows == 1) 0.25 else if (flows > 4.0) log_b(3.0, 4.25) else log_b(3.0, flows)))
    }
}
