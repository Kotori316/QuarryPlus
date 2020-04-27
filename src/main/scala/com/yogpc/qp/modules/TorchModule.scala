package com.yogpc.qp.modules

import com.yogpc.qp.gui.GuiQuarryLevel.YLevel
import com.yogpc.qp.tile.IModule.{AfterBreak, Done, NoAction, Result}
import com.yogpc.qp.tile.{IModule, TileAdvQuarry, TileQuarry, TileQuarry2}
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.EnumSkyBlock

class TorchModule(val y: () => Int) extends IModule {
  override def id = TorchModule.id

  override val calledWhen: Set[IModule.ModuleType] = Set(IModule.TypeAfterBreak)

  private[this] var lastPlaced = 0L

  /**
    * @return false if work hasn't finished.
    */
  override def action(when: IModule.CalledWhen): Result = {
    when match {
      case AfterBreak(world, pos, _, time, _) =>
        if (pos.getY == y.apply() && time != lastPlaced) {
          // Check light value
          val light = world.getLightFor(EnumSkyBlock.BLOCK, pos.up())
          val state = world.getBlockState(pos.down())
          if (light < 9 && state.getBlock.canPlaceTorchOnTop(state, world, pos)) {
            world.setBlockState(pos, Blocks.TORCH.getDefaultState)
            lastPlaced = time
            Done
          } else {
            NoAction
          }
        } else {
          NoAction
        }
      case _ => NoAction
    }
  }


  override def toString = s"TorchModule(${y.apply()})"
}

object TorchModule {
  final val id = "quarryplus:module_torch"

  def get(t: TileEntity): TorchModule = {
    val getter = t match {
      case quarry2: TileQuarry2 => () => implicitly[YLevel[TileQuarry2]].getYLevel(quarry2)
      case quarry: TileQuarry => () => implicitly[YLevel[TileQuarry]].getYLevel(quarry)
      case adv: TileAdvQuarry => () => implicitly[YLevel[TileAdvQuarry]].getYLevel(adv)
      case _ => () => 0
    }
    new TorchModule(getter)
  }
}
