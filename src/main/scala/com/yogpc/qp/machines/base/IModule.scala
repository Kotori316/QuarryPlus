package com.yogpc.qp.machines.base

import cats._
import cats.implicits._
import com.yogpc.qp.machines.base.IModule.{CalledWhen, Result}
import com.yogpc.qp.machines.pump.PumpModule
import com.yogpc.qp.machines.replacer.ReplacerModule
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

trait IModule {
  def id: String

  def calledWhen: Set[IModule.ModuleType]

  def invoke(when: CalledWhen): Result = {
    if (calledWhen(when.moduleType)) {
      action(when)
    } else {
      IModule.NoAction
    }
  }

  /**
    * @return false if work hasn't finished. true if work has done or did nothing.
    */
  protected def action(when: CalledWhen): Result

  def toString: String
}

object IModule {
  implicit val moduleShow: Show[IModule] = Show.fromToString
  val getId: IModule => String = _.id
  val replaceModuleIDs = Set(ReplacerModule.id)
  val pumpModuleIDs = Set(PumpModule.ID)
  val hasReplaceModule: IModule => Boolean = getId andThen replaceModuleIDs
  val hasPumpModule: IModule => Boolean = getId andThen pumpModuleIDs

  sealed trait ModuleType

  final case object TypeBeforeBreak extends ModuleType

  final case object TypeCollectItem extends ModuleType

  final case object TypeAfterBreak extends ModuleType

  final case object TypeTick extends ModuleType

  sealed abstract class CalledWhen(val moduleType: ModuleType)

  final case class BeforeBreak(xp: Int, world: World, pos: BlockPos) extends CalledWhen(TypeBeforeBreak)

  final case class CollectingItem(entities: List[Entity]) extends CalledWhen(TypeCollectItem)

  final case class AfterBreak(world: World, pos: BlockPos, before: IBlockState) extends CalledWhen(TypeAfterBreak)

  final case class Tick(tile: APowerTile) extends CalledWhen(TypeTick)

  sealed trait Result {
    def canGoNext: Boolean = this =!= NotFinished

    def done: Boolean = this === Done
  }

  case object NoAction extends Result

  case object NotFinished extends Result

  case object Done extends Result

  object Result {
    implicit val eqOfResult: Eq[Result] = Eq.fromUniversalEquals
    implicit val monoidOfResult: Monoid[Result] = new Monoid[Result] {
      override def empty: Result = NoAction

      override def combine(x: Result, y: Result): Result = x match {
        case NoAction => y
        case NotFinished => x
        case Done =>
          if (y === NoAction) x
          else y
      }
    }
  }

}
