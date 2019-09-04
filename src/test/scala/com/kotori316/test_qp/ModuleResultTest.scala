package com.kotori316.test_qp

import cats._
import cats.implicits._
import com.yogpc.qp.machines.base.IModule
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ModuleResultTest {

  import IModule._

  val noAction: IModule.Result = NoAction
  val notFinished: IModule.Result = NotFinished
  val done: IModule.Result = Done

  @Test
  def test_empty(): Unit = {
    val monoid = Monoid[IModule.Result]
    assertTrue(monoid.empty === IModule.NoAction)
    assertTrue(monoid.empty === noAction)
  }

  @Test
  def combine(): Unit = {
    import IModule._
    val l1 = List(NoAction, Done, NotFinished, NotFinished)
    assertEquals(NotFinished, Monoid[IModule.Result].combineAll(l1))
    assertEquals(NotFinished, Monoid[IModule.Result].combineAll(l1.reverse))
    val l2: List[Result] = List(Done, NoAction, Done)
    assertEquals(Done, l2.foldMap(identity))
    val l3: List[Result] = List(NoAction, NoAction, NoAction)
    assertEquals(NoAction, Foldable[List].fold(l3))
  }

  @Test
  def associativity(): Unit = {
    assertEquals(noAction, noAction |+| noAction)
    assertEquals(done, done |+| done)
    assertEquals(notFinished, notFinished |+| notFinished)

    assertEquals((noAction |+| done) |+| done, noAction |+| (done |+| done))
    assertEquals((noAction |+| notFinished) |+| done, noAction |+| (notFinished |+| done))
    assertEquals((done |+| notFinished) |+| done, done |+| (notFinished |+| done))
  }

  @Test
  def left_identity(): Unit = {
    assertEquals(noAction, noAction |+| noAction)
    assertEquals(done, noAction |+| done)
    assertEquals(notFinished, noAction |+| notFinished)
  }

  @Test
  def right_identity(): Unit = {
    assertEquals(noAction, noAction |+| noAction)
    assertEquals(done, done |+| noAction)
    assertEquals(notFinished, notFinished |+| noAction)

  }
}
