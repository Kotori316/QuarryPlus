package com.yogpc.qp.tile

import com.yogpc.qp.tile.IModule.AfterBreak
import net.minecraft.block.state.IBlockState

class ReplacerModule(val toReplace: () => IBlockState) extends IModule {
  override val id = ReplacerModule.id

  override val calledWhen: Set[IModule.ModuleType] = Set(IModule.TypeAfterBreak)

  override def action(when: IModule.CalledWhen): Boolean = {
    when match {
      case AfterBreak(world, pos, before) =>
        val replaceState = toReplace()
        if (before != replaceState) {
          world.setBlockState(pos, replaceState)
        }
        true
      case _ => true
    }

  }

  override def toString = {
    s"ReplacerModule(${toReplace.apply()})"
  }
}

object ReplacerModule {
  final val id = "quarryplus:module_replacer"

  def apply(toReplace: IBlockState): ReplacerModule = new ReplacerModule(() => toReplace)

  def apply(replacer: TileReplacer): ReplacerModule = new ReplacerModule(replacer.getReplaceState)
}
