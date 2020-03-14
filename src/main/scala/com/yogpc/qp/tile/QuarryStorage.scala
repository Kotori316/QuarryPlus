package com.yogpc.qp.tile

import com.yogpc.qp.compat.{FluidStore, InvUtils}
import com.yogpc.qp.utils.ItemElement
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.{FluidRegistry, FluidStack}

class QuarryStorage extends INBTSerializable[NBTTagCompound] with HasStorage.Storage {

  private var items = Map.empty[ItemDamage, ItemElement]
  private type FluidUnit = Long
  private var fluids = Map.empty[FluidStack, FluidUnit]

  def addItem(stack: ItemStack): Unit = {
    val key = ItemDamage(stack)
    val inserting = ItemElement(stack)
    val element = items.getOrElse(key, ItemElement.invalid)
    items = items.updated(key, element + inserting)
    //QuarryPlus.LOGGER.debug(MARKER, s"Inserted $inserting")
  }

  def pushItem(world: World, pos: BlockPos): Unit = {
    items.headOption.foreach { case (key, element) =>
      //      (for (f <- EnumFacing.VALUES.toList;
      //            t <- Option(world.getTileEntity(pos.offset(f))).toList;
      //            cap <- Option(t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite)).toList
      //      ) yield cap)
      //        .collectFirst { case handler if ItemHandlerHelper.insertItem(handler, element.toStack, true).getCount != element.count => handler }
      //        .foreach { handler =>
      //          val remain = ItemHandlerHelper.insertItem(handler, element.toStack, false)
      //          if (remain.isEmpty)
      //            items = items - key
      //          else
      //            items = items.updated(key, ItemElement(remain))
      //        }
      val remain = InvUtils.injectToNearTile(world, pos, element.toStack)
      if (remain.isEmpty)
        items = items - key
      else
        items = items.updated(key, ItemElement(remain))

    }
  }

  def addFluid(fluid: FluidStack, amount: FluidUnit)(implicit proxy: Numeric[FluidUnit]): Unit = {
    val element = fluids.getOrElse(fluid, proxy.zero)
    fluids = fluids.updated(fluid, proxy.plus(element, amount))
    //QuarryPlus.LOGGER.debug(MARKER, s"Inserted $fluid @$amount mB")
  }

  def pushFluid(world: World, pos: BlockPos): Unit = {
    fluids.foreach { case (fluid, amount) =>
      val inserted = FluidStore.injectToNearTile(world, pos, fluid, amount)
      if (amount > inserted) {
        fluids = fluids.updated(fluid, amount - inserted)
      } else {
        fluids = fluids - fluid
      }
    }
  }

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    val itemList = items.values.map(_.toNBT).foldLeft(new NBTTagList) { case (l, tag) => l.appendTag(tag); l }
    val fluidList = fluids.map { case (fluid, amount) =>
      val tag = new NBTTagCompound
      tag.setString("name", FluidRegistry.getFluidName(fluid.getFluid))
      tag.setLong("amount", amount)
      tag
    }.foldLeft(new NBTTagList) { case (l, tag) => l.appendTag(tag); l }
    nbt.setTag("items", itemList)
    nbt.setTag("fluids", fluidList)
    nbt
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    val itemList = nbt.getTagList("items", NBT.TAG_COMPOUND)
    val fluidList = nbt.getTagList("fluids", NBT.TAG_COMPOUND)
    items = Range(0, itemList.tagCount()).map(itemList.getCompoundTagAt).map { tag =>
      val stack = new ItemStack(tag)
      stack.setCount(tag.getInteger("Count"))
      ItemElement(stack)
    }.map(e => (e.itemDamage, e)).toMap
    fluids = Range(0, fluidList.tagCount()).map(fluidList.getCompoundTagAt).flatMap { tag =>
      Option(FluidRegistry.getFluid(tag.getString("name"))).map(f => (new FluidStack(f, tag.getLong("amount").toInt), tag.getLong("amount"))).toList
    }.toMap
  }

  override def insertItem(stack: ItemStack): Unit = addItem(stack)

  override def insertFluid(fluid: FluidStack, amount: Long): Unit = addFluid(fluid, amount)

  override def toString = s"QuarryStorage(item: ${items.size}, fluids: ${fluids.size})"

  def itemSize: Int = clamp(items.valuesIterator.map(_.count).sum)

  def fluidSize: Int = clamp(fluids.valuesIterator.sum)

  private def clamp(l: Long): Int =
    if (l > Int.MaxValue) Int.MaxValue
    else if (l < Int.MinValue) Int.MinValue
    else l.toInt
}
