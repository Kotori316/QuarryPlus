package com.kotori316.test_qp

import java.util.Collections

import cats.implicits._
import com.yogpc.qp.machines.base.IMarker
import com.yogpc.qp.machines.quarry.TileQuarry2.Area
import com.yogpc.qp.machines.quarry.{QuarryAction, TileQuarry2}
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class TileQuarry2Test {
  val m1 = Marker(new BlockPos(-5, 2, 8), new BlockPos(25, 9, 18))
  val m2 = Marker(new BlockPos(-5, 2, 8), new BlockPos(25, 3, 18))

  @Test
  def defaultArea(): Unit = {
    assertEquals(Area(-2, 0, 4, 8, 4, 15), TileQuarry2.defaultArea(new BlockPos(3, 0, 2), EnumFacing.SOUTH))
  }

  @Test
  def posArea(): Unit = {
    TileQuarry2.areaFromMarker(EnumFacing.SOUTH, new BlockPos(12, 2, 7), m1) match {
      case (area, marker) =>
        assertEquals(Area(-5, 2, 8, 25, 9, 18), area)
        assertTrue(marker.isDefined)
    }
    TileQuarry2.areaFromMarker(EnumFacing.SOUTH, new BlockPos(12, 2, 7), m2) match {
      case (area, marker) =>
        assertEquals(Area(-5, 2, 8, 25, 5, 18), area)
        assertTrue(marker.isDefined)
    }
    TileQuarry2.areaFromMarker(EnumFacing.SOUTH, new BlockPos(12, 2, 12), m2) match {
      case (_, marker) =>
        assertFalse(marker.isDefined)
    }
  }

  @Test
  def testShowPos(): Unit = {
    import com.yogpc.qp._
    assertEquals("(0, 0, 0)", BlockPos.ORIGIN.show)
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

  case class Marker(override val min: BlockPos, override val max: BlockPos) extends IMarker {
    override def hasLink = true

    override def removeFromWorldWithItem() = Collections.emptyList()
  }

}
