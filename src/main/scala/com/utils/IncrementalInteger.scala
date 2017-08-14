package com.utils

/**
  * Created by Alessandro on 11/08/2017.
  */
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