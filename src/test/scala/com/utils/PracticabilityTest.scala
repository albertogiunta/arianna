package com.utils

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class PracticabilityTest extends FunSuite with BeforeAndAfter {
    
    test("Testing zeros of the Practicability Function...") {
        assert(Practicability(capacity = 20, load = 0, flows = 2) == 0)
    }
    
    test("Testing upper-bound of the Practicability Function...") {
        assert(Practicability(capacity = 20, load = 0, flows = 0) == Double.PositiveInfinity)
    }
    
    test("Testing function progression...") {
        (1 to 20).foreach(i => assert(Practicability(capacity = 20, load = i, flows = 1) > Practicability(capacity = 20, load = i, flows = 2)))
    }
}
