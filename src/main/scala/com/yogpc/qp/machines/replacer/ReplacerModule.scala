package com.yogpc.qp.machines.replacer

import cats.{Always, Eval, Now}
import com.yogpc.qp.machines.base.IModule
import com.yogpc.qp.machines.base.IModule.{AfterBreak, Done, NoAction}
import net.minecraft.block.BlockState

class ReplacerModule(val toReplace: Eval[BlockState]) extends IModule {
  override val id: String = ReplacerModule.id

  override val calledWhen = Set(IModule.TypeAfterBreak)

  override def action(when: IModule.CalledWhen): IModule.Result = {
    when match {
      case AfterBreak(world, pos, before, _) =>
        val replaceState = toReplace.value
        if (before != replaceState) {
          world.setBlockState(pos, replaceState)
        }
        Done
      case _ => NoAction
    }

  }

  override def toString: String = {
    toReplace match {
      case Now(value) => s"ReplacerModule($value)"
      case value: Always[_] => s"ReplacerModule(${value.value})"
      case other => s"ReplacerModule($other)"
    }
  }
}

object ReplacerModule {
  final val id = "quarryplus:module_replacer"

  def apply(toReplace: BlockState): ReplacerModule = new ReplacerModule(Eval.now(toReplace))

  def apply(replacer: TileReplacer): ReplacerModule = new ReplacerModule(Eval.always(replacer.getReplaceState))
}
