package com.yogpc.qp.machines.advpump

import cats.Eval
import cats.data._
import com.yogpc.qp._
import com.yogpc.qp.machines.base.HasStorage
import com.yogpc.qp.utils.FluidElement
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundNBT, INBT, ListNBT}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler}

import scala.collection.immutable.ListMap
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

class TankAdvPump(capacity: Eval[Int]) extends HasStorage.Storage with IFluidHandler with INBTSerializable[CompoundNBT] {

  import TankAdvPump._

  private[this] final var fluidStacks = new ListMap[FluidElement, Int]
  private[this] final val allUnder = capacity.map(c => fluidStacks.forall { case (_, i) => i <= c / 2 })
  var amountPumped = 0L

  def canPump: Boolean = allUnder.value

  override def insertItem(stack: ItemStack): Unit = {
    // Do Nothing.
  }

  /**
   * Add the fluid to inventory.
   *
   * @param fluidStack to be inserted.
   */
  override def insertFluid(fluidStack: FluidStack): Unit = {
    if (!fluidStack.isEmpty) {
      val key = FluidElement.fromStack(fluidStack)
      val amount = fluidStacks.getOrElse(key, 0) + fluidStack.getAmount
      fluidStacks = fluidStacks.updated(key, amount)
      amountPumped += fluidStack.getAmount
    }
  }

  override def getTanks: Int = fluidStacks.size

  override def getFluidInTank(tank: Int): FluidStack = {
    if (0 > tank || tank >= getTanks) return FluidStack.EMPTY
    val (e, amount) = fluidStacks.iterator.drop(tank).next()
    e.withAmount(amount.toInt)
  }

  override def getTankCapacity(tank: Int): Int = capacity.value

  override def isFluidValid(tank: Int, stack: FluidStack) = true

  override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = 0 // Not fill-able

  override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = {
    if (resource.isEmpty || resource.getAmount <= 0) return FluidStack.EMPTY
    val key = FluidElement.fromStack(resource)
    val current = fluidStacks.getOrElse(key, 0)
    val drainAmount = Math.min(current, resource.getAmount)
    if (action.execute() && drainAmount > 0) {
      val amount = current - drainAmount
      if (amount > 0)
        fluidStacks = fluidStacks.updated(key, amount)
      else
        fluidStacks -= key
    }
    key.withAmount(drainAmount)
  }

  override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = {
    if (maxDrain <= 0) return FluidStack.EMPTY
    fluidStacks.headOption match {
      case Some((key, current)) =>
        val drainAmount = Math.min(current, maxDrain)
        if (action.execute() && drainAmount > 0) {
          val amount = current - drainAmount
          if (amount > 0)
            fluidStacks = fluidStacks.updated(key, amount)
          else
            fluidStacks -= key
        }
        key.withAmount(drainAmount)
      case None => FluidStack.EMPTY
    }
  }

  def getCapability[T](cap: Capability[T]): Cap[T] = Cap.make(cap, this, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)

  override def serializeNBT(): CompoundNBT = {
    val nbt = new CompoundNBT
    val list = fluidStacks.toSeq.map { case (element, i) => element.withAmount(i).toNBT }.foldLeft(new ListNBT) { case (l, c) => l.add(c); l }
    nbt.put(NBT_fluidStacks, list)
    nbt.put(NBT_amountPumped, amountPumped.toNBT)
    nbt
  }

  override def deserializeNBT(nbt: CompoundNBT): Unit = {
    val list = nbt.getList(NBT_fluidStacks, NBT.TAG_COMPOUND)
    fluidStacks = fluidStacks.empty
    fluidStacks ++= list.asScala.map(cast andThen loadStack andThen separate run)
    amountPumped = nbt.getLong(NBT_amountPumped)
  }
}

object TankAdvPump {
  private val NBT_capacity = "capacity"
  private val NBT_fluidStacks = "fluidStacks"
  private val NBT_amountPumped = "amountPumped"

  private val cast = Reader((n: INBT) => n.asInstanceOf[CompoundNBT])
  private val loadStack = Reader(FluidStack.loadFluidStackFromNBT)
  private val separate = Reader((stack: FluidStack) => FluidElement.fromStack(stack) -> stack.getAmount)
}