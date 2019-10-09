package com.yogpc.qp.machines.advquarry

import cats.Show
import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.compat.{FluidStore, InvUtils}
import com.yogpc.qp.machines.base.HasStorage
import com.yogpc.qp.utils.ProxyCommon.toInt
import com.yogpc.qp.utils.{FluidElement, ItemDamage, ItemElement}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundNBT, ListNBT}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack

import scala.collection.mutable

class AdvStorage extends HasStorage.Storage with INBTSerializable[CompoundNBT] {

  import AdvQuarryWork.MARKER

  val itemMap = new mutable.HashMap[ItemDamage, ItemElement]()
  val fluidMap = new mutable.HashMap[FluidElement, Long]()

  override def insertItem(stack: ItemStack): Unit = {
    if (!stack.isEmpty) {
      val key = ItemDamage.apply(stack)
      val value = itemMap.getOrElse(key, ItemElement.invalid) + ItemElement.apply(stack)
      itemMap.update(key, value)
    }
  }

  def insertItems(stacks: scala.collection.Seq[ItemStack], log: Boolean = false): Unit = {
    stacks.foreach(insertItem)
    if (log && stacks.nonEmpty)
      QuarryPlus.LOGGER.debug(MARKER, s"Inserted ${stacks.mkString(""",""")}")
  }

  def pushItem(world: World, pos: BlockPos): Unit = {
    itemMap.headOption.foreach { case (key, element) =>
      val remain = InvUtils.injectToNearTile(world, pos, element.toStack)
      if (remain.isEmpty)
        itemMap -= key
      else
        itemMap.update(key, ItemElement(remain))
    }
  }

  /**
   * Add the fluid to inventory.
   *
   * @param fluidStack to be inserted.
   */
  override def insertFluid(fluidStack: FluidStack): Unit = {
    insertFluid(fluidStack, log = true)
  }

  def insertFluid(fluidStack: FluidStack, log: Boolean): Unit = {
    if (!fluidStack.isEmpty) {
      val key = FluidElement.fromStack(fluidStack)
      val value = fluidMap.getOrElse(key, 0L) + fluidStack.getAmount
      fluidMap.update(key, value)
      if (log)
        QuarryPlus.LOGGER.debug(MARKER, s"Inserted ${fluidStack.show}")
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

  def addAll(that: AdvStorage, log: Boolean): Unit = {
    that.itemMap.foreach { case (key, value) => this.itemMap.update(key, this.itemMap.getOrElse(key, ItemElement.invalid) + value) }
    that.fluidMap.foreach { case (key, value) => this.fluidMap.update(key, this.fluidMap.getOrElse(key, 0L) + value) }
    if (log && that.itemMap.nonEmpty) {
      QuarryPlus.LOGGER.debug(MARKER, s"Inserted ${that.itemMap.values.mkString(""",""")}")
    }
    if (log && that.fluidMap.nonEmpty) {
      //      QuarryPlus.LOGGER.debug(MARKER, s"Inserted ${that.fluidMap.map { case (element, l) => element.show + '@' + l }.mkString(""",""")}")
    }
  }

  override def serializeNBT(): CompoundNBT = {
    val nbt = new CompoundNBT
    val itemList = this.itemMap.values.map(_.toNBT).foldLeft(new ListNBT) { case (l, data) => l.add(data); l }
    nbt.put("items", itemList)
    val fluidList = this.fluidMap.toSeq.map { case (element, l) =>
      val data = element.toNBT
      data.putLong("amount", l)
      data
    }.foldLeft(new ListNBT) { case (l, data) => l.add(data); l }
    nbt.put("fluids", fluidList)
    nbt
  }

  override def deserializeNBT(nbt: CompoundNBT): Unit = {
    this.itemMap.clear()
    this.fluidMap.clear()
    import scala.jdk.CollectionConverters._
    val itemList = nbt.getList("items", NBT.TAG_COMPOUND)
    itemList.asScala.map { case tag: CompoundNBT =>
      val stack = ItemStack.read(tag)
      stack.setCount(1)
      val count = tag.getLong("Count")
      new ItemElement(ItemDamage(stack), count)
    }.foreach(e => itemMap.update(e.itemDamage, e))
    val fluidList = nbt.getList("fluids", NBT.TAG_COMPOUND)
    fluidMap ++=
      fluidList.asScala.map { case tag: CompoundNBT =>
        val key = FluidElement.fromNBT(tag)
        val amount = tag.getLong("amount")
        key -> amount
      }
  }
}

object AdvStorage {
  implicit val showAdvStorage: Show[AdvStorage] = s =>
    s"AdvStorage(item: ${s.itemMap.size}, fluids: ${s.fluidMap.size})"
}
