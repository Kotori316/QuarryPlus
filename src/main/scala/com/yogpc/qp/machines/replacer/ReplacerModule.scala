package com.yogpc.qp.machines.replacer

import cats.{Always, Eval, Now}
import com.yogpc.qp.machines.base.IModule
import com.yogpc.qp.machines.base.IModule.AfterBreak
import net.minecraft.block.state.IBlockState

class ReplacerModule(val toReplace: Eval[IBlockState]) extends IModule {
  override def id = ReplacerModule.id

  override def calledWhen = Set(IModule.TypeAfterBreak)

  override def action(when: IModule.CalledWhen): Unit = {
    when match {
      case AfterBreak(world, pos, before) =>
        val replaceState = toReplace.value
        if (before != replaceState) {
          world.setBlockState(pos, replaceState)
        }
      case _ =>
    }
  }

  override def toString = {
    toReplace match {
      case Now(value) => s"ReplacerModule($value)"
      case value: Always[_] => s"ReplacerModule(${value.value})"
      case other => s"ReplacerModule($other)"
    }
  }
}

object ReplacerModule {
  final val id = "quarryplus:module_replacer"

  def apply(toReplace: IBlockState): ReplacerModule = new ReplacerModule(Eval.now(toReplace))

  def apply(replacer: TileReplacer): ReplacerModule = new ReplacerModule(Eval.always(replacer.getReplaceState))
}
