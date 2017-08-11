package com.utils

final case class IncrementalInteger(private var base: Int) {
    
    
    def ++ : Int = {
        base += 1
        this.value
    }
    
    def -- : Int = {
        base -= 1
        this.value
    }
    
    def +=(step: Int): Int = {
        base += step
        this.value
    }
    
    def -=(step: Int): Int = {
        base -= step
        this.value
    }
    
    def value: Int = base
    
    override def toString: String = this.base.toString
}

object IncrementalInteger {
    
    implicit def fromIntToIncremental(from: Int): IncrementalInteger = IncrementalInteger(from: Int)
    
    implicit def fromIncrementalToInt(incrementalInteger: IncrementalInteger): Int = incrementalInteger.value
}

object TryIt extends App {
    
    import IncrementalInteger._
    
    val i = IncrementalInteger(0)
    
    val x: IncrementalInteger = i ++
    
    val y: Int = x
    
    println(i, x, y ++)
}