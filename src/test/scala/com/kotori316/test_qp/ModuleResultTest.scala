package com.kotori316.test_qp

import cats._
import cats.implicits._
import com.yogpc.qp.machines.base.IModule
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ModuleResultTest {

  @Test
  def test_empty(): Unit = {
    val monoid = Monoid[IModule.Result]
    assertTrue(monoid.empty === IModule.NoAction)
  }

  @Test
  def combine(): Unit = {
    import IModule._
    val l1 = List(NoAction, Done, NotFinished, NotFinished)
    assertEquals(NotFinished, Monoid[IModule.Result].combineAll(l1))
    val l2: List[Result] = List(Done, NoAction, Done)
    assertEquals(Done, l2.foldMap(identity))
    val l3: List[Result] = List(NoAction, NoAction, NoAction)
    assertEquals(NoAction, Foldable[List].fold(l3))
  }
}
