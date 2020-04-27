package com.yogpc.qp.tile

import com.yogpc.qp.integration.bedrockore.BedrockOreModule
import com.yogpc.qp.modules.TorchModule
import com.yogpc.qp.tile.IModule.{CalledWhen, Result}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.Loader

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
  val getId: IModule => String = _.id
  val replaceModuleIDs = Set(ReplacerModule.id, TorchModule.id)
  val pumpModuleIDs = Set(PumpModule.ID)
  val expPumpModuleIDs = Set(ExpPumpModule.ID)
  val hasReplaceModule: IModule => Boolean = getId andThen replaceModuleIDs
  val hasPumpModule: IModule => Boolean = getId andThen pumpModuleIDs
  val hasExpPumpModule: IModule => Boolean = getId andThen expPumpModuleIDs
  val replaceBlocks: Int => PartialFunction[IModule, List[IBlockState]] = y => {
    case t: TorchModule if t.y() == y => List(Blocks.TORCH.getDefaultState)
    case r: ReplacerModule => List(r.toReplace())
    case _ => Nil
  }

  def defaultModules(tile: APowerTile): List[IModule] = {
    if (Loader.isModLoaded(BedrockOreModule.bedrock_mod_id)) {
      List(BedrockOreModule.from(tile))
    } else {
      Nil
    }
  }

  sealed trait ModuleType

  final case object TypeBeforeBreak extends ModuleType

  final case object TypeCollectItem extends ModuleType

  final case object TypeAfterBreak extends ModuleType

  final case object TypeTick extends ModuleType

  sealed abstract class CalledWhen(val moduleType: ModuleType)

  final case class BeforeBreak(world: World, pos: BlockPos) extends CalledWhen(TypeBeforeBreak)

  final case class CollectingItem(entities: List[Entity]) extends CalledWhen(TypeCollectItem)

  final case class AfterBreak(world: World, pos: BlockPos, before: IBlockState, worldTime: Long, xp: Int) extends CalledWhen(TypeAfterBreak)

  final case class Tick(tile: APowerTile) extends CalledWhen(TypeTick)

  sealed trait Result {
    def canGoNext: Boolean = this != NotFinished

    def done: Boolean = this == Done
  }

  case object NoAction extends Result

  case object NotFinished extends Result

  case object Done extends Result

  object Result {
    def combine(x: Result, y: Result): Result = x match {
      case NoAction => y
      case NotFinished => x
      case Done =>
        if (y == NoAction) x
        else y
    }
  }

}
