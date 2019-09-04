package com.yogpc.qp.machines.advquarry

import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.compat.FluidStore
import com.yogpc.qp.machines.base.HasStorage
import com.yogpc.qp.utils.ProxyCommon.toInt
import com.yogpc.qp.utils.{FluidElement, ItemDamage, ItemElement}
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.items.{CapabilityItemHandler, ItemHandlerHelper}

import scala.collection.mutable

class AdvStorage extends HasStorage.Storage {

  import AdvQuarryWork.MARKER

  val itemMap = new mutable.HashMap[ItemDamage, ItemElement]()
  val fluidMap = new mutable.HashMap[FluidElement, Long]()

  override def insertItem(stack: ItemStack): Unit = {
    if (!stack.isEmpty) {
      val key = ItemDamage.apply(stack)
      val value = itemMap.getOrElse(key, ItemElement.invalid) + ItemElement.apply(stack)
      itemMap.update(key, value)
      QuarryPlus.LOGGER.trace(MARKER, s"Inserted $stack")
    }
  }

  def pushItem(world: World, pos: BlockPos): Unit = {
    itemMap.headOption.foreach { case (key, element) =>
      (for (f <- facings.value;
            t <- Option(world.getTileEntity(pos.offset(f))).toList;
            cap <- t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite).asScala.value.value.toList
            ) yield cap)
        .collectFirst { case handler if ItemHandlerHelper.insertItem(handler, element.toStack, true).getCount != element.count => handler }
        .foreach { handler =>
          val remain = ItemHandlerHelper.insertItem(handler, element.toStack, false)
          if (remain.isEmpty)
            itemMap -= key
          else
            itemMap.update(key, ItemElement(remain))
        }
    }
  }

  /**
   * Add the fluid to inventory.
   *
   * @param fluidStack to be inserted.
   */
  override def insertFluid(fluidStack: FluidStack): Unit = {
    if (!fluidStack.isEmpty) {
      val key = FluidElement.fromStack(fluidStack)
      val value = fluidMap.getOrElse(key, 0L) + fluidStack.getAmount
      fluidMap.update(key, value)
      QuarryPlus.LOGGER.trace(MARKER, s"Inserted ${fluidStack.show}")
    }
  }

  def pushFluid(world: World, pos: BlockPos): Unit = {
    fluidMap.foreach { case (element, amount) =>
      val inserted = FluidStore.injectToNearTile(world, pos, element.withAmount(toInt(amount)))
      if (amount > inserted) {
        fluidMap.update(element, amount - inserted)
      } else {
        fluidMap -= element
      }
    }
  }

}
