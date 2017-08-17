package com.utils

object Practicability {
    
    val log_b: (Double, Double) => Double = (b, n) => Math.log(n) / Math.log(b)
    
    def apply(capacity: Double, load: Double, flows: Double): Double = {
        if (flows == 0) Double.PositiveInfinity
        else if (load == 0) 0.0
        else load / capacity * (if (flows == 1) 1.25 else if (flows == 2) 1.0 else if (flows == 3.0) 0.875 else 0.75)
    }
    
    def toWeight(from: Double, to: Double): Double = to
}