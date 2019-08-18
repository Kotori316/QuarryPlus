package com.yogpc.qp.machines.modules

import cats._
import cats.implicits._
import com.yogpc.qp.machines.advquarry.TileAdvQuarry
import com.yogpc.qp.machines.base.IModule
import com.yogpc.qp.machines.base.IModule.{AfterBreak, Done, NoAction, Result}
import com.yogpc.qp.machines.item.GuiQuarryLevel
import com.yogpc.qp.machines.quarry.{TileQuarry, TileQuarry2}
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.EnumLightType

class TorchModule(val y: Eval[Int]) extends IModule {
  override def id = TorchModule.id

  override val calledWhen = Set(IModule.TypeAfterBreak)

  /**
    * @return false if work hasn't finished.
    */
  override def action(when: IModule.CalledWhen): Result = {
    when match {
      case AfterBreak(world, pos, _) =>
        if (pos.getY === y.value) {
          // Check light value
          val light = world.getLightFor(EnumLightType.BLOCK, pos.up())
          if (light < 9 && world.getBlockState(pos.down()).canPlaceTorchOnTop(world, pos.down())) {
            world.setBlockState(pos, Blocks.TORCH.getDefaultState)
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
