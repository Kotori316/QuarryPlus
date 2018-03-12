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

import com.yogpc.qp.tile.{IEnchantableTile, TileBasic}
import com.yogpc.qp.{Config, QuarryPlus, QuarryPlusI, _}
import net.minecraft.client.resources.I18n
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Enchantments, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand}
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.oredict.OreDictionary

object ItemTool extends Item with IEnchantableItem {
    final val meta_statuschecker = 0
    final val meta_listeditor = 1
    final val meta_liquidselector = 2
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
    final val NAME_key = "Bname"
    final val META_key = "Bmeta"
    val item = this

    setMaxStackSize(1)
    setHasSubtypes(true)
    setMaxDamage(0)
    setCreativeTab(QuarryPlusI.creativeTab)
    setUnlocalizedName(QuarryPlus.Names.tool)
    setRegistryName(QuarryPlus.modID, QuarryPlus.Names.tool)

    override def isBookEnchantable(itemstack1: ItemStack, itemstack2: ItemStack) = false

    def onItemUseFirst(player: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand,
                       facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
        val returnValue = if (!worldIn.isRemote) EnumActionResult.SUCCESS else EnumActionResult.PASS
        val stack = player.getHeldItem(hand)
        if (stack.getItemDamage == meta_listeditor) {
            val nbttl = stack.getEnchantmentTagList
            val s = Option(nbttl).toList.flatMap(_.tagIterator).exists(_.getShort("id") == IEnchantableTile.SilktouchID)
            val f = Option(nbttl).toList.flatMap(_.tagIterator).exists(_.getShort("id") == IEnchantableTile.FortuneID)
            var stackTag: NBTTagCompound = stack.getTagCompound
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
                            val datas = if (f) tb.fortuneList else tb.silktouchList
                            datas.add(bd)
                        }
                        stackTag.removeTag(NAME_key)
                        stackTag.removeTag(META_key)
                    } else {
                        player.openGui(QuarryPlus.INSTANCE, if (f) QuarryPlusI.guiIdFList else QuarryPlusI.guiIdSList, worldIn, pos.getX, pos.getY, pos.getZ)
                    }
                    return returnValue
                case _ =>
            }
            if (!state.getBlock.isAir(state, worldIn, pos)) {
                if (stackTag == null) {
                    stackTag = new NBTTagCompound
                    stack.setTagCompound(stackTag)
                }
                val key = ForgeRegistries.BLOCKS.getKey(state.getBlock)
                assert(key != null, "Unregistered Block?")
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
        super.onItemUse(player.getHeldItem(hand), player, worldIn, pos, hand, facing, hitX, hitY, hitZ)
    }

    override def onItemUseFirst(stack: ItemStack, player: EntityPlayer, world: World, pos: BlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand): EnumActionResult = {
        onItemUseFirst(player, world, pos, hand, side, hitX, hitY, hitZ)
    }

    override def onItemUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
        super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)
    }

    override def getUnlocalizedName(is: ItemStack) =
        is.getItemDamage match {
            case ItemTool.meta_listeditor =>
                "item." + ItemTool.listeditor
            case ItemTool.meta_liquidselector =>
                "item." + ItemTool.liquidselector
            case ItemTool.meta_statuschecker =>
                "item." + ItemTool.statuschecker
            case _ => super.getUnlocalizedName(is)
        }

    @SideOnly(Side.CLIENT)
    override def addInformation(stack: ItemStack, playerIn: EntityPlayer, tooltip: util.List[String], advanced: Boolean): Unit =
        if (stack.getItemDamage == meta_listeditor) {
            val c = stack.getTagCompound
            if (c != null) {
                if (c.hasKey(NAME_key)) {
                    tooltip.add(c.getString(NAME_key))
                    val meta = c.getInteger(META_key)
                    if (meta != OreDictionary.WILDCARD_VALUE) tooltip.add(meta.toString)
                }
                val enchantments = ItemTool.getEnchantmentMap(stack)
                if (enchantments.getOrElse(Enchantments.FORTUNE, 0) > 0) tooltip.add(I18n.format(Enchantments.FORTUNE.getName))
                else if (enchantments.getOrElse(Enchantments.SILK_TOUCH, 0) > 0) tooltip.add(I18n.format(Enchantments.SILK_TOUCH.getName))
            }
        }

    override def getSubItems(itemIn: Item, tab: CreativeTabs, items: java.util.List[ItemStack]): Unit = {
        items.add(new ItemStack(this, 1, meta_statuschecker))
        items.add(getEditorStack)
        items.add(new ItemStack(this, 1, meta_liquidselector))
        if (Config.content.debug /*&& QuarryPlus.instance.inDev*/ ) {
            val stack = new ItemStack(Items.DIAMOND_PICKAXE)
            stack.addEnchantment(Enchantments.EFFICIENCY, 5)
            stack.addEnchantment(Enchantments.UNBREAKING, 3)

            {
                val stack1 = stack.copy
                stack1.addEnchantment(Enchantments.FORTUNE, 3)
                items.add(stack1)
            }
            {
                val stack1 = stack.copy
                stack1.addEnchantment(Enchantments.SILK_TOUCH, 1)
                items.add(stack1)
            }
        }
    }

    override def canMove(is: ItemStack, enchantment: Enchantment): Boolean = {
        if (is.getItemDamage != meta_listeditor) return false
        val l = is.getEnchantmentTagList
        (l == null || l.tagCount == 0) && ((enchantment eq Enchantments.SILK_TOUCH) || (enchantment eq Enchantments.FORTUNE))
    }

    private def getEnchantmentMap(stack: ItemStack): Map[Enchantment, Int] = {
        val nbttaglist = stack.getEnchantmentTagList
        if (nbttaglist == null) {
            Map.empty
        } else {
            Range(0, nbttaglist.tagCount()).map(i => {
                val tag = nbttaglist.getCompoundTagAt(i)
                (Enchantment.getEnchantmentByID(tag.getShort("id")), tag.getShort("lvl").toInt)
            }).toMap
        }
    }

    def getEditorStack = {
        val stack = new ItemStack(this, 1, meta_listeditor)
        val compound = new NBTTagCompound
        compound.setInteger("HideFlags", 1)
        stack.setTagCompound(compound)
        stack
    }
}