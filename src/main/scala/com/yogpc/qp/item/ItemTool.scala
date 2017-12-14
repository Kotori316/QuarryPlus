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
 *//*
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
import javax.annotation.Nullable

import com.yogpc.qp.tile.{IEnchantableTile, TileBasic}
import com.yogpc.qp.{BlockData, Config, QuarryPlus, QuarryPlusI}
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Enchantments, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.oredict.OreDictionary

object ItemTool extends Item with IEnchantableItem {
    /**
      * meta=1
      */
    val listeditor = "listeditor"
    /**
      * meta=2
      */
    val liquidselector = "liquidselector"
    /**
      * meta=0
      */
    val statuschecker = "statuschecker"
    val NAME_key = "Bname"
    val META_key = "Bmeta"
    val item = this

    setMaxStackSize(1)
    setHasSubtypes(true)
    setMaxDamage(0)
    setCreativeTab(QuarryPlusI.ct)
    setUnlocalizedName(QuarryPlus.Names.tool)
    setRegistryName(QuarryPlus.modID, QuarryPlus.Names.tool)

    override def isBookEnchantable(itemstack1: ItemStack, itemstack2: ItemStack) = false

    override def onItemUse(player: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand,
                           facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
        val stack = player.getHeldItem(hand)
        if (stack.getItemDamage == 1) {
            var s = false
            var f = false
            val nbttl = stack.getEnchantmentTagList
            if (nbttl != null) {
                for (i <- 0 until nbttl.tagCount()) {
                    val id = nbttl.getCompoundTagAt(i).getShort("id")
                    if (id == IEnchantableTile.SilktouchID) s = true
                    if (id == IEnchantableTile.FortuneID) f = true
                }
            }
            var c = stack.getTagCompound
            val state = worldIn.getBlockState(pos)
            var bd: BlockData = null
            if (c != null && c.hasKey(ItemTool.NAME_key)) {
                if (state.getBlock.isAir(state, worldIn, pos)) {
                    c.removeTag(ItemTool.NAME_key)
                    c.removeTag(ItemTool.META_key)
                    return EnumActionResult.SUCCESS
                }
                bd = new BlockData(c.getString(ItemTool.NAME_key), c.getInteger(ItemTool.META_key))
            }
            worldIn.getTileEntity(pos) match {
                case tb: TileBasic if s != f =>
                    if (c != null && bd != null) {
                        if (!worldIn.isRemote)
                            (if (f) tb.fortuneList else tb.silktouchList).add(bd)
                        c.removeTag(ItemTool.NAME_key)
                        c.removeTag(ItemTool.META_key)
                    } else if (!worldIn.isRemote)
                        player.openGui(QuarryPlus.INSTANCE, if (f) QuarryPlusI.guiIdFList else QuarryPlusI.guiIdSList, worldIn, pos.getX, pos.getY, pos.getZ)
                    return EnumActionResult.SUCCESS
                case _ =>
            }
            if (!state.getBlock.isAir(state, worldIn, pos)) {
                if (c == null) {
                    c = new NBTTagCompound
                    stack.setTagCompound(c)
                }
                val key = ForgeRegistries.BLOCKS.getKey(state.getBlock)
                assert(key != null, "Unregistered Block?")
                val name = key.toString
                val meta = state.getBlock.getMetaFromState(state)
                if (c.hasKey(ItemTool.NAME_key) && name == c.getString(ItemTool.NAME_key) && meta == c.getInteger(ItemTool.META_key))
                    c.setInteger(ItemTool.META_key, OreDictionary.WILDCARD_VALUE)
                else {
                    c.setString(ItemTool.NAME_key, name)
                    c.setInteger(ItemTool.META_key, meta)
                }
            }
        }
        super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ)
    }

    override def getUnlocalizedName(is: ItemStack) =
        is.getItemDamage match {
            case 1 =>
                "item." + ItemTool.listeditor
            case 2 =>
                "item." + ItemTool.liquidselector
            case _ => "item." + ItemTool.statuschecker
        }

    @SideOnly(Side.CLIENT)
    override def addInformation(stack: ItemStack, @Nullable worldIn: World, tooltip: util.List[String], flagIn: ITooltipFlag) =
        if (stack.getItemDamage == 1) {
            val c = stack.getTagCompound
            if (c != null) {
                if (c.hasKey(ItemTool.NAME_key)) {
                    tooltip.add(c.getString(ItemTool.NAME_key))
                    val meta = c.getInteger(ItemTool.META_key)
                    if (meta != OreDictionary.WILDCARD_VALUE) tooltip.add(Integer.toString(meta))
                }
                val enchantments = EnchantmentHelper.getEnchantments(stack)
                if (enchantments.getOrDefault(Enchantments.FORTUNE, 0) > 0) tooltip.add(I18n.format(Enchantments.FORTUNE.getName))
                else if (enchantments.getOrDefault(Enchantments.SILK_TOUCH, 0) > 0) tooltip.add(I18n.format(Enchantments.SILK_TOUCH.getName))
            }
        }

    override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]) = if (isInCreativeTab(tab)) {
        items.add(new ItemStack(this, 1, 0))
        items.add(getEditorStack)
        items.add(new ItemStack(this, 1, 2))
        if (Config.content.debug && QuarryPlus.instance.inDev) {
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
        if (is.getItemDamage != 1) return false
        val l = is.getEnchantmentTagList
        (l == null || l.tagCount == 0) && ((enchantment eq Enchantments.SILK_TOUCH) || (enchantment eq Enchantments.FORTUNE))
    }

    def getEditorStack = {
        val stack = new ItemStack(this, 1, 1)
        val compound = new NBTTagCompound
        compound.setInteger("HideFlags", 1)
        stack.setTagCompound(compound)
        stack
    }
}