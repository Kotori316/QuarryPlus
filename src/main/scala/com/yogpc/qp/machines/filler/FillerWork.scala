package com.yogpc.qp.machines.filler

import cats._
import cats.data._
import cats.implicits._
import com.yogpc.qp.machines.PowerManager
import com.yogpc.qp.machines.base.Area
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

trait FillerWork {
  def isWorking: Boolean

  def tick(area: Area, world: World, tile: FillerTile): FillerWork.Out[FillerWork]
}

object FillerWork {
  type Out[A] = WriterT[Eval, Chain[String], A]

  object Waiting extends FillerWork {
    override def isWorking: Boolean = false

    override def tick(area: Area, world: World, tile: FillerTile): FillerWork.Out[FillerWork] =
      Monad[Out].pure(this)
  }

  abstract class Fill extends FillerWork {
    override def isWorking: Boolean = true

    protected def targetIterator(area: Area): Iterator[BlockPos]
  }

  def selectBlock(tile: FillerTile): Out[Option[(ItemStack, Block)]] = {
    for {
      opt <- WriterT.valueT(Eval.always(tile.inventory.firstBlock()))
      _ <- WriterT.tell(Chain(s"Selected $opt."))
    } yield opt
  }

  def consumeEnergy(tile: FillerTile): Out[Boolean] = {
    val consume = Eval.always(PowerManager.useEnergyFillerWork(tile, false))
    WriterT(consume product consume.map(b => if (b) "Energy used." else "Energy runs out.").map(Monad[Chain].pure))
  }

  def placeBlock(world: World, pos: BlockPos, block: Block): Out[Unit] = {
    val state = block.getDefaultState
    for {
      result <- WriterT.valueT(Eval.always(world.setBlockState(pos, state)))
      _ <- WriterT.tell(Chain(if (result) s"Placed $state at $pos." else s"Failed to place $state at $pos."))
    } yield ()
  }
}
