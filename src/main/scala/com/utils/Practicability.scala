package com.utils

object Practicability {
    
    val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
    
    def apply(capacity: Double, load: Double, flows: Double): Double = {
        if (flows == 0) Double.PositiveInfinity
        else load / capacity * (
            if (flows == 1) log_b(3.0, 4.5)
            else if (flows == 2) log_b(3.0, 3.0)
            else if (flows == 3.0) log_b(3.0, 2.65)
            else log_b(3.0, 2.15)
            )
    }
    
    def toWeight(from: Double, to: Double): Double = to
    
    def round(value: Double): Double = BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
}

object TryPracticability extends App {
    
    println(Practicability(100.0, 1.0, 2.0))
}