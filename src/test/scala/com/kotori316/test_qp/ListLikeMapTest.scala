package com.kotori316.test_qp

import com.yogpc.qp.utils.ListLikeMap
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ListLikeMapTest {

  @Test def `empty object is empty`(): Unit = {
    val e = ListLikeMap.empty[String, Int]
    assertTrue(e.isEmpty)
  }

  @Test def init1(): Unit = {
    val m1 = ListLikeMap("5" -> 5, "a" -> 1, "1" -> 1, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2)
    assertEquals(7, m1.size)
    val l = m1.toList
    assertEquals(List("5" -> 5, "a" -> 1, "1" -> 1, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2), l)

    l.map(_._2).zip(m1.valuesIterator).foreach { case (i, i1) => assertEquals(i, i1) }
  }

  @Test def `update and keep order`(): Unit = {
    val m1 = ListLikeMap("5" -> 5, "a" -> 1, "1" -> 1, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2)
    val keys = m1.map { case (k, _) => k }
    val m2 = m1.updated("1", 11)
    assertEquals(ListLikeMap("5" -> 5, "a" -> 1, "1" -> 1, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2), m1, "Immutable test.")
    assertEquals(ListLikeMap("5" -> 5, "a" -> 1, "1" -> 11, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2), m2, "Update value and keep order.")
    assertEquals(keys, m2.map { case (k, _) => k })
  }

  @Test def foldLeftTest(): Unit = {
    val m1 = ListLikeMap("5" -> 5, "a" -> 1, "1" -> 1, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2)
    val v = m1.foldLeft("") { case (z, (k, v)) => z + k + v }
    assertEquals("55a111b233c322", v)
  }

  @Test def foldRightTest(): Unit = {
    val m1 = ListLikeMap("5" -> 5, "a" -> 1, "1" -> 1, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2)
    val v = m1.foldRight("") { case ((k, v), s) => k + v + s }
    assertEquals("55a111b233c322", v)
    val v2 = m1.foldRight("") { case ((k, v), s) => s + k + v }
    assertEquals("22c333b211a155", v2)
  }
}
