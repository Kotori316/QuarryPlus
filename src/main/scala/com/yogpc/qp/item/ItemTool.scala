/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.yogpc.qp.item

import java.util

import com.yogpc.qp.tile.{IEnchantableTile, TileBasic, TileQuarry2}
import com.yogpc.qp.utils.BlockData
import com.yogpc.qp.{Config, QuarryPlus, QuarryPlusI, _}
import javax.annotation.Nullable
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Enchantments, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.oredict.OreDictionary

class ItemTool extends Item with IEnchantableItem {

  import ItemTool._

  setMaxStackSize(1)
  setHasSubtypes(true)
  setMaxDamage(0)
  setCreativeTab(QuarryPlusI.creativeTab)
  setUnlocalizedName(QuarryPlus.Names.tool)
  setRegistryName(QuarryPlus.modID, QuarryPlus.Names.tool)

  override def isBookEnchantable(s1: ItemStack, s2: ItemStack) = false

  override def onItemUseFirst(player: EntityPlayer, worldIn: World, pos: BlockPos, side: EnumFacing,
                              hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand): EnumActionResult = {
    val returnValue = if (!worldIn.isRemote) EnumActionResult.SUCCESS else EnumActionResult.PASS
    val stack = player.getHeldItem(hand)
    if (stack.getItemDamage == meta_ListEditor) {
      val enchantSet = stack.getEnchantmentTagList.tagIterator.map(_.getShort("id").toInt).toSet
      val s = enchantSet.contains(IEnchantableTile.SilktouchID)
      val f = enchantSet.contains(IEnchantableTile.FortuneID)
      var stackTag = stack.getTagCompound
      val state = worldIn.getBlockState(pos)
      var bd: BlockData = null
      if (stackTag != null && stackTag.hasKey(NAME_key)) {
        bd = new BlockData(stackTag.getString(NAME_key), stackTag.getInteger(META_key))
        if (player.isSneaking && bd == new BlockData(state.getBlock, state)) {
          stackTag.removeTag(NAME_key)
          stackTag.removeTag(META_key)
          return returnValue
        }
      }
      worldIn.getTileEntity(pos) match {
        case tb: TileBasic if s != f =>
          if (stackTag != null && bd != null) {
            if (!worldIn.isRemote) {
              val data = if (f) tb.fortuneList else tb.silktouchList
              data.add(bd)
            }
            stackTag.removeTag(NAME_key)
            stackTag.removeTag(META_key)
          } else {
            player.openGui(QuarryPlus.INSTANCE, if (f) QuarryPlusI.guiIdFList else QuarryPlusI.guiIdSList, worldIn, pos.getX, pos.getY, pos.getZ)
          }
          return returnValue
        case quarry2: TileQuarry2 if s != f =>
          if (stackTag != null && bd != null) {
            if (!worldIn.isRemote) {
              if (f) {
                quarry2.fortuneList += bd
              } else {
                quarry2.silktouchList += bd
              }
            }
            stackTag.removeTag(NAME_key)
            stackTag.removeTag(META_key)
          } else {
            // player.openGui(QuarryPlus.INSTANCE, if (f) QuarryPlusI.guiIdFList else QuarryPlusI.guiIdSList, worldIn, pos.getX, pos.getY, pos.getZ)
            if (!worldIn.isRemote) {
              val modeString = (include: Boolean) => if (include) "WhiteList mode" else "BlackList mode"
              player.sendStatusMessage(new TextComponentString(s"Fortune: ${modeString(quarry2.fortuneInclude)}"), false)
              player.sendStatusMessage(new TextComponentString(s"Silktouch: ${modeString(quarry2.silktouchInclude)}"), false)
              val data = if (f) quarry2.fortuneList else quarry2.silktouchList
              if (data.nonEmpty) player.sendStatusMessage(new TextComponentString("----Blocks----"), false)
              data.map(_.getLocalizedName).foreach(s => player.sendStatusMessage(new TextComponentString(s), false))
            }
          }
          return EnumActionResult.SUCCESS
        case _ =>
      }
      if (!state.getBlock.isAir(state, worldIn, pos)) {
        if (stackTag == null) {
          stackTag = new NBTTagCompound
          stack.setTagCompound(stackTag)
        }
        val key = ForgeRegistries.BLOCKS.getKey(state.getBlock)
        require(key != null, "The item must be registered.")
        val name = key.toString
        val meta = state.getBlock.getMetaFromState(state)
        if (stackTag.hasKey(NAME_key) && name == stackTag.getString(NAME_key) && meta == stackTag.getInteger(META_key))
          stackTag.setInteger(META_key, OreDictionary.WILDCARD_VALUE)
        else {
          stackTag.setString(NAME_key, name)
          stackTag.setInteger(META_key, meta)
        }
        return returnValue
      }
    }
    super.onItemUseFirst(player, worldIn, pos, side, hitX, hitY, hitZ, hand)
  }

  override def getUnlocalizedName(stack: ItemStack) =
    stack.getItemDamage match {
      case ItemTool.`meta_ListEditor` =>
        "item." + ItemTool.listeditor
      case ItemTool.`meta_LiquidSelector` =>
        "item." + ItemTool.liquidselector
      case ItemTool.`meta_StatusChecker` =>
        "item." + ItemTool.statuschecker
      case ItemTool.meta_YSetter =>
        "item." + ItemTool.ySetter
      case _ => super.getUnlocalizedName(stack)
    }

  @SideOnly(Side.CLIENT)
  override def addInformation(stack: ItemStack, @Nullable worldIn: World, tooltip: util.List[String], flagIn: ITooltipFlag): Unit =
    if (stack.getItemDamage == meta_ListEditor) {
      val tag = stack.getTagCompound
      if (tag != null) {
        if (tag.hasKey(NAME_key)) {
          tooltip.add(tag.getString(NAME_key))
          val meta = tag.getInteger(META_key)
          if (meta != OreDictionary.WILDCARD_VALUE)
            tooltip.add(meta.toString)
        }
        val enchantments = ItemTool.getEnchantmentMap(stack)
        if (enchantments.getOrElse(Enchantments.FORTUNE, 0) > 0)
          tooltip.add(I18n.format(Enchantments.FORTUNE.getName))
        else if (enchantments.getOrElse(Enchantments.SILK_TOUCH, 0) > 0)
          tooltip.add(I18n.format(Enchantments.SILK_TOUCH.getName))
      }
    }

  override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]): Unit = if (isInCreativeTab(tab)) {
    items.add(new ItemStack(this, 1, meta_StatusChecker))
    items.add(getEditorStack)
    items.add(new ItemStack(this, 1, meta_LiquidSelector))
    items.add(new ItemStack(this, 1, meta_YSetter))
  }

  override def canMove(is: ItemStack, enchantment: Enchantment): Boolean = {
    if (is.getItemDamage != meta_ListEditor) return false
    val l = is.getEnchantmentTagList
    (l == null || l.tagCount == 0) && ((enchantment eq Enchantments.SILK_TOUCH) || (enchantment eq Enchantments.FORTUNE))
  }

  override def stacks(): Array[ItemStack] = Array(getEditorStack)

  override def isValidInBookMover: Boolean = false
}

object ItemTool {
  final val meta_StatusChecker = 0
  final val meta_ListEditor = 1
  final val meta_LiquidSelector = 2
  final val meta_YSetter = 3
  /**
    * meta=1
    */
  final val listeditor = "listeditor"
  /**
    * meta=2
    */
  final val liquidselector = "liquidselector"
  /**
    * meta=0
    */
  final val statuschecker = "statuschecker"
  /**
    * meta=3
    */
  final val ySetter = "y_setter"
  final val NAME_key = "Bname"
  final val META_key = "Bmeta"

  private def getEnchantmentMap(stack: ItemStack): Map[Enchantment, Int] = {
    (for (enchList <- Option(stack.getEnchantmentTagList).iterator;
          tag <- enchList.tagIterator) yield {
      Enchantment.getEnchantmentByID(tag.getShort("id")) -> tag.getShort("lvl").toInt
    }).toMap
  }

  def getEditorStack: ItemStack = {
    val stack = new ItemStack(QuarryPlusI.itemTool, 1, meta_ListEditor)
    val compound = new NBTTagCompound
    compound.setInteger("HideFlags", 1)
    stack.setTagCompound(compound)
    stack
  }
}