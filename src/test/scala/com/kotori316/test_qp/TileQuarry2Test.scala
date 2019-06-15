package com.kotori316.test_qp

import com.yogpc.qp.machines.quarry.TileQuarry2
import com.yogpc.qp.machines.quarry.TileQuarry2.Area
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class TileQuarry2Test {

  @Test
  def defaultArea(): Unit = {
    assertEquals(Area(-2, 0, 3, 8, 3, 14), TileQuarry2.defaultArea(new BlockPos(3, 0, 2), EnumFacing.SOUTH))
  }
}
