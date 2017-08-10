package com.utils

/**
  * Created by Alessandro on 05/07/2017.
  */
case class Counter(private val start: Int) {
    
    private var value = start
    
    def ++ : Int = {
        value += 1
        this.get
    }
    
    def ++(step: Int): Int = {
        value += step
        this.get
    }
    
    def -- : Int = {
        value -= 1;
        this.get
    }
    
    def --(step: Int): Int = {
        value -= step
        this.get
    }
    
    def ++==(that: Int): Boolean = (this ++) == that
    
    def --==(that: Int): Boolean = (this --) == that
    
    def get: Int = value
    
    override def equals(obj: scala.Any) = obj match {
        case that: Int => that == this.get
        case that: Counter => that.get == this.get
        case _ => false
    }
}

object Counter {

    def apply(): Counter = new Counter(0)

    implicit def counterToInt(counter: Counter): Int = counter.get

    implicit def intToCounter(n: Int): Counter = new Counter(n)
}
