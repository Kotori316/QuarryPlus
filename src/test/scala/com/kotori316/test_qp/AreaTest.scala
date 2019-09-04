package com.kotori316.test_qp

import com.yogpc.qp._
import com.yogpc.qp.machines.base.Area
import jp.t2v.lab.syntax.MapStreamSyntax
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class AreaTest {

  val alwaysTrue = MapStreamSyntax.always_true[Any]().asScala
  val alwaysFalse = MapStreamSyntax.always_false[Any]().asScala
  val area = Area(8, 16, 9, 12, 16, 29)

  @Test
  def dummy(): Unit = {
    assertTrue(true)
    assertFalse(false)
    assertNotEquals(Area.zeroArea, area)
  }

  @Test
  def poses(): Unit = {
    val zeroPoses = Area.posesInArea(Area.zeroArea, alwaysTrue, alwaysTrue, alwaysTrue)
    assertTrue(zeroPoses.nonEmpty)
    assertEquals(1, zeroPoses.size)
    val inArea = Area.posesInArea(area, alwaysTrue, alwaysTrue, alwaysTrue)
    assertTrue(inArea.nonEmpty)
  }

  @Test
  def emptyArea(): Unit = {
    assertTrue(Area.posesInArea(area, alwaysFalse, alwaysFalse, alwaysFalse).isEmpty)
    assertTrue(Area.posesInArea(area, _ > 15, alwaysTrue, alwaysTrue).isEmpty)
  }

  @Test
  def filter(): Unit = {
    val a = new BlockPos(14, 80, 65)
    val b = new BlockPos(36, 80, 91)
    val area = Area.posToArea(a, b)
    val poses = Area.posesInArea(area, alwaysTrue, alwaysTrue, alwaysTrue)
    assertTrue(poses forall (_.getY == 80))
    assertTrue(poses contains a)
    assertTrue(poses contains b)
    assertTrue(poses contains new BlockPos(24, 80, 76))
  }
}
