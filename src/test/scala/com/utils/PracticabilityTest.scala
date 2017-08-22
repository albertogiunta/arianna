package com.utils

import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by Xander_C on 17/08/2017.
  */
class PracticabilityTest extends FunSuite with BeforeAndAfter {
    
    test("Testing zeros of the Practicability Function...") {
        assert(Practicability(capacity = 20, load = 0, flows = 2) == 0)
    }
    
    test("Testing upper-bound of the Practicability Function...") {
        assert(Practicability(capacity = 20, load = 0, flows = 0) == Double.PositiveInfinity)
    }
    
    test("Testing function progression...") {
    
        (1 to 20).foreach(i => assert(Practicability(capacity = 20, load = i, flows = 1) > Practicability(capacity = 20, load = i, flows = 2)))
        (1 to 20).foreach(i => assert(Practicability(capacity = 20, load = i, flows = 2) > Practicability(capacity = 20, load = i, flows = 3)))
        (1 to 20).foreach(i => assert(Practicability(capacity = 20, load = i, flows = 3) > Practicability(capacity = 20, load = i, flows = 4)))
        (1 to 20).foreach(i => assert(Practicability(capacity = 20, load = i, flows = 4) < Practicability(capacity = 20, load = i, flows = 1)))
    }
}
