package com.yogpc.qp.machines.base

import cats._
import cats.implicits._
import com.yogpc.qp.machines.base.IModule.{CalledWhen, Result}
import com.yogpc.qp.machines.modules.TorchModule
import com.yogpc.qp.machines.pump.PumpModule
import com.yogpc.qp.machines.replacer.ReplacerModule
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.apache.logging.log4j.{Marker, MarkerManager}

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
  val MARKER: Marker = MarkerManager.getMarker("QUARRY_MODULE")
  implicit val moduleShow: Show[IModule] = Show.fromToString
  val getId: IModule => String = _.id
  val hasReplaceModule: IModule => Boolean = has(ReplacerModule.id, TorchModule.id)
  val hasPumpModule: IModule => Boolean = has(PumpModule.ID)
  val replaceBlocks: Int => IModule => List[BlockState] = y => {
    case t: TorchModule if t.y.contains_(y) => List(Blocks.TORCH.getDefaultState)
    case r: ReplacerModule => r.toReplace.toList
    case _ => Nil
  }

  def has(id: String, ids: String*): IModule => Boolean = {
    val set = ids.toSet + id
    getId andThen set
  }

  sealed trait ModuleType

  final case object TypeBeforeBreak extends ModuleType

  final case object TypeCollectItem extends ModuleType

  final case object TypeAfterBreak extends ModuleType

  final case object TypeTick extends ModuleType

  sealed abstract class CalledWhen(val moduleType: ModuleType)

  final case class BeforeBreak(world: World, pos: BlockPos) extends CalledWhen(TypeBeforeBreak)

  final case class CollectingItem(entities: List[Entity]) extends CalledWhen(TypeCollectItem)

  /**
   * The return value of this type is whether replace work is finished.
   */
  final case class AfterBreak(world: World, pos: BlockPos, before: BlockState, time: Long, xp: Int) extends CalledWhen(TypeAfterBreak)

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
