package com.yogpc.qp.machines.modules

import com.yogpc.qp.Config
import com.yogpc.qp.machines.PowerManager
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.quarry.ContainerQuarryModule.HasModuleInventory
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.item.ItemStack
import net.minecraft.world.dimension.DimensionType

class RemoveBedrockModule(tile: APowerTile with HasStorage with HasModuleInventory) extends IModule {
  override def id: String = RemoveBedrockModule.id

  override def calledWhen: Set[IModule.ModuleType] = Set(IModule.TypeBeforeBreak)

  /**
   * @return false if work hasn't finished. true if work has done or did nothing.
   */
  override protected def action(when: IModule.CalledWhen): IModule.Result = {
    when match {
      case IModule.BeforeBreak(_, world, pos) =>
        val state = world.getBlockState(pos)
        val toRemove = world.getDimension.getType match {
          case DimensionType.THE_NETHER => (pos.getY > 0 && pos.getY < 5) || (pos.getY > 122 && pos.getY < 127)
          case _ => pos.getY > 0 && pos.getY < 5
        }
        if (toRemove && Config.common.removeBedrock.get() && state.getBlock == Blocks.BEDROCK) {
          val energy = RemoveBedrockModule.calcUnbreakableEnergy(state, List(this))
          if (PowerManager.useEnergy(tile, energy, EnergyUsage.BREAK_BLOCK)) {
            if (Config.common.collectBedrock.get()) {
              tile.getStorage.insertItem(new ItemStack(Blocks.BEDROCK))
            }
            val replace = tile.getModules.flatMap(IModule.replaceBlocks(pos.getY)).headOption.getOrElse(Blocks.AIR.getDefaultState)
            world.setBlockState(pos, replace, 0x10 | 0x3)
            IModule.Done
          } else {
            IModule.NotFinished
          }
        } else {
          IModule.NoAction
        }
      case _ => IModule.NoAction
    }
  }

  override def toString = "RemoveBedrockModule"
}

object RemoveBedrockModule {
  final val id = "quarryplus:module_bedrock"

  def calcUnbreakableEnergy(state: BlockState, modules: List[IModule]): Long = {
    if (state.getBlock == Blocks.BEDROCK && modules.exists(IModule.has(RemoveBedrockModule.id))) {
      if (Config.common.collectBedrock.get()) {
        PowerManager.calcEnergyBreak(50f, EnchantmentHolder.noEnch)
      } else {
        PowerManager.calcEnergyBreak(100f, EnchantmentHolder.noEnch) * 2
      }
    } else {
      0L
    }
  }

}
