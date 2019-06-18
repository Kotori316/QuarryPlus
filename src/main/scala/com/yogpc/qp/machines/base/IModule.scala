package com.yogpc.qp.machines.base

import com.yogpc.qp.machines.base.IModule.CalledWhen
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

trait IModule {
  type Accessor

  def calledWhen: Set[Class[_ <: IModule.CalledWhen]]

  def invoke(when: CalledWhen): Unit = {
    if (calledWhen(when.getClass)) {
      action(when)
    }
  }

  def access: Option[Accessor]

  def action(when: CalledWhen): Unit
}

object IModule {

  sealed trait CalledWhen

  case class OnBreak(xp: Int, world: World, pos: BlockPos) extends CalledWhen

  case class DropItem(xp: Int) extends CalledWhen

  case class CollectingItem(entities: List[_ <: Entity]) extends CalledWhen

}