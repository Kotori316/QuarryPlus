package com.kotori316.test_qp

import cats._
import cats.data._
import cats.implicits._
import com.yogpc.qp._
import net.minecraftforge.common.util.LazyOptional
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class CapOptionTest {
  implicit val e1: Eq[Integer] = Eq.fromUniversalEquals

  @Test
  def dummy(): Unit = {
    assertEquals(5, 1 + 4)
  }

  @Test
  def transformSome(): Unit = {
    var tick = 0
    val opt = LazyOptional.of[Integer](() => {
      tick += 1
      Int.box(tick)
    })
    assertEquals(0, tick)
    val option = transform0(opt)
    assertEquals(0, tick)

    assertEquals(option.value.get, Int.box(1))
    assertEquals(opt.orElse(Int.box(3)), Int.box(1))
    assertEquals(tick, 1)
  }

  @Test
  def transNone(): Unit = {
    val opt = LazyOptional.empty[String]()
    val option = transform0(opt)
    assert(option.value.isEmpty)
  }

  @Test
  def makeInvalid(): Unit = {
    val opt = LazyOptional.of[String](() => "Tame")
    val eval = opt.asScala
    val s = for (st <- eval) yield st |+| "_go"
    val s2 = eval.map( _ |+| "_go")

    val getter = Kleisli((d: String) => s.getOrElse(d))
    assertEquals(getter("Valid").value, "Tame_go")
    assertEquals(s.getOrElse("").value, s2.getOrElse("").value)

    opt.invalidate()
    assertEquals(getter("invalidate").value, "invalidate")
    assertFalse(opt.isPresent)
  }

  @Test
  def filter(): Unit = {
    val o1 = LazyOptional.of[String](() => "a").asScala
    val o2 = LazyOptional.of[String](() => "a" * 8).asScala
    val a8 = List(o1, o2).map(_.filter(_.length > 5)).flatMap(_.value.value)
    assertEquals(1, a8.length)
  }
}
