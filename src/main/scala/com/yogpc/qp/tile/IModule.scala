package com.yogpc.qp.tile

import com.yogpc.qp.tile.IModule.CalledWhen
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

trait IModule {
  def id: String

  def calledWhen: Set[IModule.ModuleType]

  def invoke(when: CalledWhen): Boolean = {
    if (calledWhen(when.moduleType)) {
      action(when)
    } else {
      true
    }
  }

  /**
    * @return false if work hasn't finished.
    */
  def action(when: CalledWhen): Boolean

  def toString: String
}

object IModule {
  val getId: IModule => String = _.id
  val replaceModuleIDs = Set(ReplacerModule.id)
  val pumpModuleIDs = Set(PumpModule.ID)
  val hasReplaceModule: IModule => Boolean = getId andThen replaceModuleIDs
  val hasPumpModule: IModule => Boolean = getId andThen pumpModuleIDs

  sealed trait ModuleType

  final case object TypeBeforeBreak extends ModuleType

  final case object TypeCollectItem extends ModuleType

  final case object TypeAfterBreak extends ModuleType

  sealed abstract class CalledWhen(val moduleType: ModuleType)

  final case class BeforeBreak(xp: Int, world: World, pos: BlockPos) extends CalledWhen(TypeBeforeBreak)

  final case class CollectingItem(entities: List[Entity]) extends CalledWhen(TypeCollectItem)

  final case class AfterBreak(world: World, pos: BlockPos, before: IBlockState) extends CalledWhen(TypeAfterBreak)

}
