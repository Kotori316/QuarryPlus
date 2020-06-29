package com.kotori316.test_qp

import cats.implicits._
import com.yogpc.qp.machines.base.{Area, IMarker}
import com.yogpc.qp.machines.quarry.QuarryAction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{Direction, ResourceLocation}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class TileQuarry2Test {
  val m1 = Marker(new BlockPos(-5, 2, 8), new BlockPos(25, 9, 18))
  val m2 = Marker(new BlockPos(-5, 2, 8), new BlockPos(25, 3, 18))
  val m3 = Marker(new BlockPos(0, 63, 0), new BlockPos(15, 68, 15))
  val m4 = Marker(new BlockPos(8, 5, 8), new BlockPos(10, 5, 16))
  val dimID = new ResourceLocation("minecraft:over_world").some

  @Test
  def defaultArea(): Unit = {
    assertEquals(Area(-2, 0, 4, 8, 3, 14, dimID), Area.defaultQuarryArea(new BlockPos(3, 0, 2), Direction.SOUTH, dimID))
  }

  @Test
  def posArea(): Unit = {
    Area.areaFromMarker(Direction.SOUTH, new BlockPos(12, 2, 7), m1, dimID) match {
      case (area, marker) =>
        assertEquals(Area(-5, 2, 8, 25, 9, 18, dimID), area)
        assertTrue(marker.isDefined)
    }
    Area.areaFromMarker(Direction.SOUTH, new BlockPos(12, 2, 7), m2, dimID) match {
      case (area, marker) =>
        assertEquals(Area(-5, 2, 8, 25, 5, 18, dimID), area)
        assertTrue(marker.isDefined)
    }
    Area.areaFromMarker(Direction.SOUTH, new BlockPos(12, 2, 12), m2, dimID) match {
      case (_, marker) =>
        assertFalse(marker.isDefined)
    }
  }

  @Test
  def digTargets(): Unit = {
    val pos = new BlockPos(-1, 1, 5)
    val (area, m) = Area.areaFromMarker(Direction.WEST, pos, m3, dimID)
    assertTrue(m.isDefined)
    val poses = QuarryAction.digTargets(area, pos, 63, log = false)
    assertEquals((m3.max.getX - m3.min.getX - 1) * (m3.max.getZ - m3.min.getZ - 1), poses.size)
  }

  @Test
  def nearDigTargets(): Unit = {
    {
      val marker = Marker(m4.min(), m4.max().east())
      val pos = new BlockPos(7, 5, 8)
      val (area, m) = Area.areaFromMarker(Direction.EAST, pos, marker, dimID)
      assertTrue(m.isDefined)
      val poses = QuarryAction.digTargets(area, pos, pos.getY, log = false)
      assertFalse(poses.isEmpty)
      assertEquals(14, poses.size)
    }
    {
      val marker = m4
      val pos = new BlockPos(7, 5, 8)
      val (area, m) = Area.areaFromMarker(Direction.EAST, pos, marker, dimID)
      assertTrue(m.isDefined)
      val poses = QuarryAction.digTargets(area, pos, pos.getY, log = false)
      assertFalse(poses.isEmpty)
      assertEquals(7, poses.size)
      assertEquals(9, poses.head.getX)
    }
  }

  @Test
  def signum(): Unit = {
    val signum = QuarryAction.signum
    assertEquals(1, signum(1, 5))
    assertEquals(-1, signum(-1, -5))
    assertEquals(1, signum(5, 5))
  }

  @Test
  def insideFrame(): Unit = {
    val (area, _) = Area.areaFromMarker(Direction.SOUTH, new BlockPos(12, 2, 7), m1, dimID)
    assertEquals(Area(-5, 2, 8, 25, 9, 18, dimID), area)
    val poses = QuarryAction.insideFrameArea(area)
    assertEquals(m1.max.getX, poses.maxBy(_.getX).getX)
    assertEquals(m1.max.getZ, poses.maxBy(_.getZ).getZ)
    assertEquals((m1.max.getX - m1.min.getX + 1) * (m1.max.getZ - m1.min.getZ + 1) * (m1.max.getY - m1.min.getY + 1), poses.size)
  }

  @Test
  def testShowPos(): Unit = {
    import com.yogpc.qp._
    assertEquals("(0, 0, 0)", BlockPos.ZERO.show)
    assertEquals("(1, 0, 4)", new BlockPos(1, 0, 4).show)
  }

  @Test
  def nearFar(): Unit = {
    val x0 = 0
    val x1 = 10
    val x2 = 15
    assertEquals(x0, QuarryAction.near(1, x0, x1))
    assertEquals(x1, QuarryAction.far(1, x0, x1))
    assertEquals(x0, QuarryAction.near(5, x0, x1))
    assertEquals(x1, QuarryAction.far(5, x0, x1))
    assertEquals(x0, QuarryAction.near(5, x0, x2))
    assertEquals(x2, QuarryAction.far(5, x0, x2))
    assertEquals(x0, QuarryAction.near(7, x0, x2))
    assertEquals(x2, QuarryAction.far(7, x0, x2))

    assertEquals(6, QuarryAction.near(10, 6, 14))
    assertEquals(14, QuarryAction.far(10, 6, 14))
  }

  object Marker {
    def apply(min: BlockPos, max: BlockPos): IMarker = new IMarker.MarkerImpl(min, max)
  }

}
