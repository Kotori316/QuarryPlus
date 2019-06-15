package com.yogpc.qp.machines.quarry

import com.yogpc.qp._
import com.yogpc.qp.compat.FluidStore
import com.yogpc.qp.utils.{ItemDamage, ItemElement}
import net.minecraft.fluid.Fluid
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.items.{CapabilityItemHandler, ItemHandlerHelper}

class QuarryStorage {
  private var items = Map.empty[ItemDamage, ItemElement]
  private type FluidUnit = Long
  private var fluids = Map.empty[Fluid, FluidUnit]

  def addItem(stack: ItemStack): Unit = {
    val key = ItemDamage(stack)
    val element = items.getOrElse(key, ItemElement(stack))
    items = items.updated(key, element)
  }

  def pushItem(world: World, pos: BlockPos): Unit = {
    items.headOption.foreach { case (key, element) =>
      facings.value.flatMap(f => world.getTileEntity(pos.offset(f)).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite).asScala.value.value)
        .collectFirst { case handler if ItemHandlerHelper.insertItem(handler, element.toStack, true).getCount != element.count => handler }
        .foreach { handler =>
          ItemHandlerHelper.insertItem(handler, element.toStack, false)
          items = items - key
        }
    }
  }

  def addFluid(fluid: Fluid, amount: FluidUnit): Unit = {
    val element = fluids.getOrElse(fluid, amount)
    fluids = fluids.updated(fluid, element)
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
}
