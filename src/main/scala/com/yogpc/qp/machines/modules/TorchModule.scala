package com.yogpc.qp.machines.modules

import cats._
import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.machines.advquarry.TileAdvQuarry
import com.yogpc.qp.machines.base.IModule
import com.yogpc.qp.machines.base.IModule.{AfterBreak, Done, NoAction, Result}
import com.yogpc.qp.machines.item.GuiQuarryLevel
import com.yogpc.qp.machines.quarry.{TileQuarry, TileQuarry2}
import net.minecraft.block.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.LightType

class TorchModule(val y: Eval[Int]) extends IModule {
  override def id = TorchModule.id

  override val calledWhen = Set(IModule.TypeAfterBreak)

  private[this] var lastPlaced = 0L

  /**
   * @return false if work hasn't finished.
   */
  override def action(when: IModule.CalledWhen): Result = {
    when match {
      case AfterBreak(world, pos, _, time) =>
        if (pos.getY === y.value && lastPlaced =!= time) {
          // Check light value
          val light = world.getLightFor(LightType.BLOCK, pos.up())
          if (light < 9 && Blocks.TORCH.getDefaultState.isValidPosition(world, pos)) {
            world.setBlockState(pos, Blocks.TORCH.getDefaultState)
            lastPlaced = time
            QuarryPlus.LOGGER.debug(IModule.MARKER, f"Torch Module found light=$light%02d at ${pos.show} and placed torch")
            Done
          } else {
            if (light < 10) QuarryPlus.LOGGER.debug(IModule.MARKER, f"Torch Module found light=$light%02d at ${pos.show} but didn't place")
            NoAction
          }
        } else {
          NoAction
        }
      case _ => NoAction
    }
  }


  override def toString = s"TorchModule(${y.value})"
}

object TorchModule {
  final val id = "quarryplus:module_torch"

  def get(t: TileEntity): TorchModule = {
    import GuiQuarryLevel._
    val getter = t match {
      case quarry2: TileQuarry2 => Eval.always(implicitly[YLevel[TileQuarry2]].getYLevel(quarry2))
      case quarry: TileQuarry => Eval.always(implicitly[YLevel[TileQuarry]].getYLevel(quarry))
      case adv: TileAdvQuarry => Eval.always(implicitly[YLevel[TileAdvQuarry]].getYLevel(adv))
      case _ => Eval.Zero
    }
    new TorchModule(getter)
  }
}
