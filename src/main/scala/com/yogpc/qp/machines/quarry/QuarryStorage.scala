package com.yogpc.qp.machines.quarry

import cats._
import com.yogpc.qp._
import com.yogpc.qp.compat.FluidStore
import com.yogpc.qp.machines.base.HasStorage
import com.yogpc.qp.utils.{ItemDamage, ItemElement}
import net.minecraft.fluid.Fluid
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.IRegistry
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.{CapabilityItemHandler, ItemHandlerHelper}

class QuarryStorage extends INBTSerializable[NBTTagCompound] with HasStorage.Storage {

  import QuarryAction.MARKER

  private var items = Map.empty[ItemDamage, ItemElement]
  private type FluidUnit = Long
  private var fluids = Map.empty[Fluid, FluidUnit]

  def addItem(stack: ItemStack): Unit = {
    val key = ItemDamage(stack)
    val inserting = ItemElement(stack)
    val element = items.getOrElse(key, ItemElement.invalid)
    items = items.updated(key, element + inserting)
    QuarryPlus.LOGGER.debug(MARKER, s"Inserted $inserting")
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

  def addFluid(fluid: Fluid, amount: FluidUnit)(implicit proxy: Numeric[FluidUnit]): Unit = {
    val element = fluids.getOrElse(fluid, proxy.zero)
    fluids = fluids.updated(fluid, proxy.plus(element, amount))
    QuarryPlus.LOGGER.debug(MARKER, s"Inserted $fluid @$amount mB")
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
    val itemList = items.values.map(_.toNBT).foldLeft(new NBTTagList) { case (l, tag) => l.add(tag); l }
    val fluidList = fluids.map { case (fluid, amount) =>
      val tag = new NBTTagCompound
      tag.putString("name", IRegistry.FLUID.getKey(fluid).toString)
      tag.putLong("amount", amount)
      tag
    }.foldLeft(new NBTTagList) { case (l, tag) => l.add(tag); l }
    nbt.put("items", itemList)
    nbt.put("fluids", fluidList)
    nbt
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    val itemList = nbt.getList("items", NBT.TAG_COMPOUND)
    val fluidList = nbt.getList("fluids", NBT.TAG_COMPOUND)
    items = Range(0, itemList.size()).map(itemList.getCompound).map { tag =>
      val stack = ItemStack.read(tag)
      stack.setCount(tag.getInt("Count"))
      stack
    }.map(ItemElement.apply).map(e => (e.itemDamage, e)).toMap
    fluids = Range(0, fluidList.size()).map(fluidList.getCompound).flatMap { tag =>
      Option(IRegistry.FLUID.get(new ResourceLocation(tag.getString("name")))).map(f => (f, tag.getLong("amount"))).toList
    }.toMap
  }

  override def insertItem(stack: ItemStack): Unit = addItem(stack)

  override def insertFluid(fluid: Fluid, amount: Long): Unit = addFluid(fluid, amount)

  override def toString = QuarryStorage.ShowQuarryStorage.show(this)
}

object QuarryStorage {
  implicit val ShowQuarryStorage: Show[QuarryStorage] = s =>
    s"QuarryStorage(item: ${s.items.size}, fluids: ${s.fluids.size})"
}
