package com.utils

class Pair[A, B](val fst: A, val snd: B) {

    override def toString: String = "Pair[" + fst + "," + snd + "]"

    private def equals(x: Any, y: Any) = (x == null && y == null) || (x != null && x == y)

    override def equals(other: Any): Boolean = other.isInstanceOf[Pair[_, _]] &&
            equals(fst, other.asInstanceOf[Pair[_, _]].fst) &&
            equals(snd, other.asInstanceOf[Pair[_, _]].snd)

    def of[A, B](a: A, b: B) = new Pair[A, B](a, b)

}
