package com.kotori316.test_qp

import java.util.concurrent.atomic.AtomicLong

import cats.Eval
import com.yogpc.qp.machines.base.APowerTile.{MJToMicroMJ => mjt}
import com.yogpc.qp.machines.base.{APowerTile, EnergyDebug3}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class EnergyDebugTest {

  import EnergyDebugTest._

  def instance(longCounter: AtomicLong): EnergyDebug3 = {
    new EnergyDebug3("test instance", 100, Eval.always(longCounter.get()), Eval.True)
  }

  @Test
  def start(): Unit = {
    val a = instance(new AtomicLong(1))
    assertFalse(a.started)
    assertEquals(Nil, a.printInfo())
    assertEquals(Nil, a.endTick())
  }

  @Test
  def ticks50Get(): Unit = {
    val counter = new AtomicLong(1)
    val a = instance(counter)
    a.start()
    for (_ <- 0 until 50) {
      a.getAndTick(10.mj)
      counter.incrementAndGet()
    }
    assertTrue(a.started)
    val result = a.printInfo()
    assertTrue(a.started)
    assertTrue(result.exists(_.contains("in 50 ticks")), s"Found 50 in ${result.mkString}")
    assertTrue(result.exists(_.contains(s"got ${10 * 50}")), s"Got in ${result.mkString}")
    assertTrue(result.exists(_.contains(s"100 RF/t")), s"Average in ${result.mkString}")
    assertFalse(result.exists(_.contains("in 100 ticks")), s"Not Found 100 in ${result.mkString}")
  }

  @Test
  def tick150Get(): Unit = {
    val counter = new AtomicLong(0)
    val a = instance(counter)
    a.start()
    counter.incrementAndGet()
    while (counter.get() <= 150) {
      val info = a.getAndTick(10.rf)
      if (counter.get() == 100) {
        assertTrue(info.nonEmpty)
        assertTrue(info.exists(_.contains("in 100 ticks")), s"Found 100 in ${info.mkString}")
        assertTrue(info.exists(_.contains(s"10 RF/t")), s"Average in ${info.mkString}")
      } else {
        assertTrue(info.isEmpty)
      }
      counter.incrementAndGet()
    }
    val result = a.finish()
    assertFalse(a.started, "Finished")
    assertTrue(result.nonEmpty, "Result should be non-empty.")
  }
}

object EnergyDebugTest {

  implicit class Converter(private val e: Int) extends AnyVal {
    def mj: Long = e * mjt

    def rf: Long = e * APowerTile.FEtoMicroJ
  }

}
