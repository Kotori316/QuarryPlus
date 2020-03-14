package com.yogpc.qp.machines.quarry

import cats._
import com.yogpc.qp._
import com.yogpc.qp.compat.{FluidStore, InvUtils}
import com.yogpc.qp.machines.base.HasStorage
import com.yogpc.qp.utils.{FluidElement, ItemDamage, ItemElement}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundNBT, ListNBT}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack

import scala.collection.immutable.ListMap

class QuarryStorage extends INBTSerializable[CompoundNBT] with HasStorage.Storage {

  private var items = ListMap.empty[ItemDamage, ItemElement]
  private var fluids = ListMap.empty[FluidElement, FluidStack]

  def addItem(stack: ItemStack): Unit = {
    val key = ItemDamage(stack)
    val inserting = ItemElement(stack)
    val element = items.getOrElse(key, ItemElement.invalid)
    items = items.updated(key, element + inserting)
    //    QuarryPlus.LOGGER.trace(MARKER, s"Inserted $inserting")
  }

  def pushItem(world: World, pos: BlockPos): Unit = {
    items.headOption.foreach { case (key, element) =>
      val remain = InvUtils.injectToNearTile(world, pos, element.toStack)
      if (remain.isEmpty)
        items = items - key
      else
        items = items.updated(key, ItemElement(remain))
    }
  }

  def addFluid(fluidStack: FluidStack)(proxy: Monoid[FluidStack]): Unit = {
    val fluid = FluidElement.fromStack(fluidStack)
    val element = fluids.getOrElse(fluid, proxy.empty)
    fluids = fluids.updated(fluid, proxy.combine(element, fluidStack))
    //    QuarryPlus.LOGGER.trace(MARKER, s"Inserted $fluid @${fluidStack.getAmount} mB")
  }

  def pushFluid(world: World, pos: BlockPos): Unit = {
    fluids.foreach { case (element, stack) =>
      val inserted = FluidStore.injectToNearTile(world, pos, stack)
      if (stack.getAmount > inserted) {
        fluids = fluids.updated(element, new FluidStack(stack, stack.getAmount - inserted))
      } else {
        fluids = fluids - element
      }
    }
  }

  override def serializeNBT(): CompoundNBT = {
    val nbt = new CompoundNBT
    val itemList = items.values.map(_.toNBT).foldLeft(new ListNBT) { case (l, tag) => l.add(tag); l }
    val fluidList = fluids.map { case (key, value) =>
      val tag = new CompoundNBT
      tag.put("name", key.toNBT)
      tag.put("amount", value.toNBT)
      tag
    }.foldLeft(new ListNBT) { case (l, tag) => l.add(tag); l }
    nbt.put("items", itemList)
    nbt.put("fluids", fluidList)
    nbt
  }

  override def deserializeNBT(nbt: CompoundNBT): Unit = {
    val itemList = nbt.getList("items", NBT.TAG_COMPOUND)
    val fluidList = nbt.getList("fluids", NBT.TAG_COMPOUND)
    items = Range(0, itemList.size()).map(itemList.getCompound).map { tag =>
      val stack = ItemStack.read(tag)
      stack.setCount(1)
      val count = tag.getLong("Count")
      new ItemElement(ItemDamage(stack), count)
    }.map(e => (e.itemDamage, e)).to(ListMap)
    fluids = Range(0, fluidList.size()).map(fluidList.getCompound).flatMap { tag =>
      val key = FluidElement.fromNBT(tag.getCompound("name"))
      val stack = FluidStack.loadFluidStackFromNBT(tag.getCompound("amount"))
      if (stack.isEmpty) Nil else List(key -> stack)
    }.to(ListMap)
  }

  override def insertItem(stack: ItemStack): Unit = addItem(stack)

  override def insertFluid(fluidStack: FluidStack): Unit = addFluid(fluidStack)(QuarryStorage.monoidFluidStack)

  override def toString = QuarryStorage.ShowQuarryStorage.show(this)

  def itemSize: Int = clamp(items.valuesIterator.map(_.count).sum)

  def fluidSize: Int = clamp(fluids.valuesIterator.map(_.getAmount.toLong).sum)

  private def clamp(l: Long): Int =
    if (l > Int.MaxValue) Int.MaxValue
    else if (l < Int.MinValue) Int.MinValue
    else l.toInt
}

object QuarryStorage {
  implicit val ShowQuarryStorage: Show[QuarryStorage] = s =>
    s"QuarryStorage(item: ${s.items.size}, fluids: ${s.fluids.size})"

  val monoidFluidStack: Monoid[FluidStack] = new Monoid[FluidStack] {
    override def empty = FluidStack.EMPTY

    override def combine(x: FluidStack, y: FluidStack) = {
      x match {
        case emp if emp.isEmpty => y
        case _ =>
          if (y.isEmpty) x
          else if (x isFluidEqual y)
            new FluidStack(x, x.getAmount + y.getAmount)
          else {
            QuarryPlus.LOGGER.error(s"Attempt to combine $x and $y. These are different kind of fluids.")
            FluidStack.EMPTY
          }
      }
    }
  }
}
