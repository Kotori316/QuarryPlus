package com.yogpc.qp.machines.quarry

import cats._
import com.yogpc.qp._
import com.yogpc.qp.compat.FluidStore
import com.yogpc.qp.machines.base.HasStorage
import com.yogpc.qp.utils.{FluidElement, ItemDamage, ItemElement}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundNBT, ListNBT}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.items.{CapabilityItemHandler, ItemHandlerHelper}

class QuarryStorage extends INBTSerializable[CompoundNBT] with HasStorage.Storage {

  import QuarryAction.MARKER

  private var items = Map.empty[ItemDamage, ItemElement]
  private var fluids = Map.empty[FluidElement, FluidStack]

  def addItem(stack: ItemStack): Unit = {
    val key = ItemDamage(stack)
    val inserting = ItemElement(stack)
    val element = items.getOrElse(key, ItemElement.invalid)
    items = items.updated(key, element + inserting)
    QuarryPlus.LOGGER.trace(MARKER, s"Inserted $inserting")
  }

  def pushItem(world: World, pos: BlockPos): Unit = {
    items.headOption.foreach { case (key, element) =>
      (for (f <- facings.value;
            t <- Option(world.getTileEntity(pos.offset(f))).toList;
            cap <- t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite).asScala.value.value.toList
            ) yield cap)
        .collectFirst { case handler if ItemHandlerHelper.insertItem(handler, element.toStack, true).getCount != element.count => handler }
        .foreach { handler =>
          val remain = ItemHandlerHelper.insertItem(handler, element.toStack, false)
          if (remain.isEmpty)
            items = items - key
          else
            items = items.updated(key, ItemElement(remain))
        }
    }
  }

  def addFluid(fluidStack: FluidStack)(proxy: Monoid[FluidStack]): Unit = {
    val fluid = FluidElement.fromStack(fluidStack)
    val element = fluids.getOrElse(fluid, proxy.empty)
    fluids = fluids.updated(fluid, proxy.combine(element, fluidStack))
    QuarryPlus.LOGGER.trace(MARKER, s"Inserted $fluid @${fluidStack.getAmount} mB")
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
      tag.put("name", key)
      tag.put("amount", value)
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
      stack.setCount(tag.getInt("Count"))
      stack
    }.map(ItemElement.apply).map(e => (e.itemDamage, e)).toMap
    fluids = Range(0, fluidList.size()).map(fluidList.getCompound).flatMap { tag =>
      val key = FluidElement.fromNBT(tag.getCompound("name"))
      val stack = FluidStack.loadFluidStackFromNBT(tag.getCompound("amount"))
      if (stack.isEmpty) Nil else List(key -> stack)
    }.toMap
  }

  override def insertItem(stack: ItemStack): Unit = addItem(stack)

  override def insertFluid(fluidStack: FluidStack): Unit = addFluid(fluidStack)(QuarryStorage.monoidFluidStack)

  override def toString = QuarryStorage.ShowQuarryStorage.show(this)
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
