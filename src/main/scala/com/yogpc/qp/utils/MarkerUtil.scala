package com.yogpc.qp.utils

import buildcraft.api.core.IAreaProvider
import buildcraft.api.tiles.TilesAPI
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.tile.IMarker
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.Loader

import javax.annotation.Nullable

object MarkerUtil {
  def getMarker: PartialFunction[TileEntity, IMarker] = if (Loader.isModLoaded(QuarryPlus.Optionals.BuildCraft_core)) {
    case m: IMarker if m.hasLink => m
    case provider: IAreaProvider => new IMarker.BCWrapper(provider)
    case t: TileEntity if t.hasCapability(TilesAPI.CAP_TILE_AREA_PROVIDER, null) => new IMarker.BCWrapper(t.getCapability(TilesAPI.CAP_TILE_AREA_PROVIDER, null))
  } else {
    case m: IMarker if m.hasLink => m
  }

  def searchMarker(world: World, machinePos: BlockPos, @Nullable machineFacing: EnumFacing): Option[IMarker] = {
    val faceList = if (machineFacing == null) EnumFacing.HORIZONTALS.toList
    else List(machineFacing.getOpposite, machineFacing.rotateY(), machineFacing.rotateYCCW())
    faceList
      .map(machinePos.offset)
      .map(world.getTileEntity)
      .collectFirst(getMarker)
  }
}
