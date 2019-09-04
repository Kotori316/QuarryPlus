package com.kotori316.test_qp

import com.yogpc.qp._
import jp.t2v.lab.syntax.MapStreamSyntax
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{BeforeEach, Test}

class PredicateConverter {
  @BeforeEach
  def setUp(): Unit = {
  }

  @Test
  def a(): Unit = {
    val a: Object => Boolean = MapStreamSyntax.always_true[Object]().asScala
    assertTrue(a(new Object))
    assertTrue(a(new AnyRef))
    assertTrue(MapStreamSyntax.always_true[Any]().asScala(5302))
  }

  @Test
  def b(): Unit = {
    val b = MapStreamSyntax.always_false[AnyRef]().asScala
    assertFalse(b(new Object))
    assertFalse(b(new AnyRef))
    assertFalse(MapStreamSyntax.always_false[Any]().asScala(41651))
  }
}
