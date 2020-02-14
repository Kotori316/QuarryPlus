package com.kotori316.test_qp

import com.yogpc.qp.machines.base.Area
import net.minecraft.util.math.BlockPos
import net.minecraft.world.dimension.DimensionType
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class AreaTest {

  val alwaysTrue: (Int, Int, Int) => Boolean = {
    case (_, _, _) => true
  }
  val alwaysFalse: (Int, Int, Int) => Boolean = {
    case (_, _, _) => false
  }
  val area = Area(8, 16, 9, 12, 16, 29, Option(0))

  @Test
  def dummy(): Unit = {
    assertTrue(true)
    assertFalse(false)
    assertNotEquals(Area.zeroArea, area)
  }

  @Test
  def poses(): Unit = {
    val zeroPoses = Area.posesInArea(Area.zeroArea, filter = alwaysTrue)
    assertTrue(zeroPoses.nonEmpty)
    assertEquals(1, zeroPoses.size)
    val inArea = Area.posesInArea(area, alwaysTrue)
    assertTrue(inArea.nonEmpty)
  }

  @Test
  def emptyArea(): Unit = {
    assertTrue(Area.posesInArea(area, alwaysFalse).isEmpty)
    assertTrue(Area.posesInArea(area, (x, _, _) => x > 15).isEmpty)
  }

  @Test
  def filter(): Unit = {
    val a = new BlockPos(14, 80, 65)
    val b = new BlockPos(36, 80, 91)
    val area = Area.posToArea(a, b, DimensionType.OVERWORLD)
    val poses = Area.posesInArea(area, alwaysTrue)
    assertTrue(poses forall (_.getY == 80))
    assertTrue(poses contains a)
    assertTrue(poses contains b)
    assertTrue(poses contains new BlockPos(24, 80, 76))
  }

  @Test
  def filter2(): Unit = {
    val poses = Area.posesInArea(area, (x, _, z) => z > 19 || x >= 10)
    assertFalse(poses contains new BlockPos(9, 16, 16))
    assertTrue(poses contains new BlockPos(9, 16, 20))
    assertTrue(poses contains new BlockPos(10, 16, 19))
  }
}
