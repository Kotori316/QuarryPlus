package com.kotori316.test_qp

import cats.syntax.validated._
import com.yogpc.qp.machines.base.Area
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class AreaTest extends InitMC {

  val alwaysTrue: (Int, Int, Int) => Boolean = {
    case (_, _, _) => true
  }
  val alwaysFalse: (Int, Int, Int) => Boolean = {
    case (_, _, _) => false
  }
  val area = Area(8, 16, 9, 12, 16, 29, Option(new ResourceLocation("minecraft:over_world")))

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
    val area = Area.posToArea(a, b, Option(new ResourceLocation("minecraft:over_world")))
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

  private def areaByPair(min: Int, max: Int): Area = {
    Area(min, 1, min, max, 10, max, Option(new ResourceLocation("minecraft:over_world")))
  }

  @Test
  def limit22(): Unit = {
    val area = areaByPair(4, 26) // Range 22
    assertAll(
      () => assertEquals(area.validNel, Area.limit(area, Int.MaxValue), "Max Limit"),
      () => assertEquals(area.validNel, Area.limit(area, 128), "8 chunks"),
      () => assertEquals(area.validNel, Area.limit(area, 32), "2 chunks"),
      () => assertEquals(area.validNel, Area.limit(area, 22), "22 blocks"),
      () => assertNotEquals(area.validNel, Area.limit(area, 21), "21 blocks"),
      () => assertTrue(Area.limit(area, 21).isInvalid, "21 blocks changed area"),
      () => assertNotEquals(area.validNel, Area.limit(area, 16), "1 chunk."),
      () => assertNotEquals(area.validNel, Area.limit(area, 1), "1 block."),
      () => assertTrue(Area.limit(area, 1).isInvalid, "1 block. Changed"),
    )
  }

  @Test
  def limit45(): Unit = {
    val area = areaByPair(-8, 24) // Range 32 = 2 chunks
    assertAll(
      () => assertEquals(area.validNel, Area.limit(area, Int.MaxValue), "Max Limit"),
      () => assertEquals(area.validNel, Area.limit(area, 128), "8 chunks"),
      () => assertEquals(area.validNel, Area.limit(area, 64), "4 chunks"),
      () => assertEquals(area.validNel, Area.limit(area, 32), "2 chunks"),
      () => assertNotEquals(area.validNel, Area.limit(area, 31), "31 blocks"),
      () => assertTrue(Area.limit(area, 31).isInvalid, "31 blocks changed area"),
      () => assertNotEquals(area.validNel, Area.limit(area, 22), "22 blocks"),
      () => assertNotEquals(area.validNel, Area.limit(area, 21), "21 blocks"),
      () => assertNotEquals(area.validNel, Area.limit(area, 16), "1 chunk."),
      () => assertNotEquals(area.validNel, Area.limit(area, 1), "1 block."),
      () => assertTrue(Area.limit(area, 1).isInvalid, "1 block. Changed"),
    )
  }
}
