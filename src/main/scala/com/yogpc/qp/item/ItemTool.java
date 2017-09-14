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

package com.yogpc.qp.item;

import java.util.List;
import java.util.Map;

import com.yogpc.qp.BlockData;
import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileBasic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * 0=> statuschecker, 1=> listeditor, 2=> liquidselector
 */
public class ItemTool extends Item implements IEnchantableItem {
    /**
     * meta=1
     */
    public static final String listeditor = "listeditor";
    /**
     * meta=2
     */
    public static final String liquidselector = "liquidselector";
    /**
     * meta=0
     */
    public static final String statuschecker = "statuschecker";
    public static final String NAME_key = "Bname";
    public static final String META_key = "Bmeta";

    public ItemTool() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(QuarryPlusI.ct);
        setUnlocalizedName(QuarryPlus.Names.tool);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.tool);
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos,
                                      EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItemDamage() == 1) {
            boolean s = false, f = false;
            final NBTTagList nbttl = stack.getEnchantmentTagList();
            if (nbttl != null)
                for (int i = 0; i < nbttl.tagCount(); i++) {
                    final short id = nbttl.getCompoundTagAt(i).getShort("id");
                    if (id == IEnchantableTile.SilktouchID)
                        s = true;
                    if (id == IEnchantableTile.FortuneID)
                        f = true;
                }
            NBTTagCompound c = stack.getTagCompound();
            IBlockState state = worldIn.getBlockState(pos);
            BlockData bd = null;
            if (c != null && c.hasKey(NAME_key)) {
                bd = new BlockData(c.getString(NAME_key), c.getInteger(META_key));
                if (state.getBlock().isAir(state, worldIn, pos)) {
                    c.removeTag(NAME_key);
                    c.removeTag(META_key);
                    return EnumActionResult.SUCCESS;
                }
            }
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof TileBasic && s != f) {
                final TileBasic tb = (TileBasic) entity;
                if (c != null && bd != null) {
                    if (!worldIn.isRemote)
                        (f ? tb.fortuneList : tb.silktouchList).add(bd);
                    c.removeTag(NAME_key);
                    c.removeTag(META_key);
                } else if (!worldIn.isRemote)
                    player.openGui(QuarryPlus.INSTANCE, f ? QuarryPlusI.guiIdFList : QuarryPlusI.guiIdSList, worldIn, pos.getX(), pos.getY(), pos.getZ());
                return EnumActionResult.SUCCESS;
            }
            if (!state.getBlock().isAir(state, worldIn, pos)) {
                if (c == null) {
                    c = new NBTTagCompound();
                    stack.setTagCompound(c);
                }
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                assert key != null; // Unregistered Block?
                final String name = key.toString();
                final int meta = state.getBlock().getMetaFromState(state);
                if (c.hasKey(NAME_key) && name.equals(c.getString(NAME_key)) && meta == c.getInteger(META_key))
                    c.setInteger(META_key, OreDictionary.WILDCARD_VALUE);
                else {
                    c.setString(NAME_key, name);
                    c.setInteger(META_key, meta);
                }
            }

        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }


    @Override
    public String getUnlocalizedName(final ItemStack is) {
        switch (is.getItemDamage()) {
            case 1:
                return "item." + listeditor;
            case 2:
                return "item." + liquidselector;
        }
        return "item." + statuschecker;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        if (stack.getItemDamage() == 1) {
            final NBTTagCompound c = stack.getTagCompound();
            if (c != null) {
                if (c.hasKey(NAME_key)) {
                    tooltip.add(c.getString(NAME_key));
                    final int meta = c.getInteger(META_key);
                    if (meta != OreDictionary.WILDCARD_VALUE)
                        tooltip.add(Integer.toString(meta));
                }
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
                if (enchantments.getOrDefault(Enchantments.FORTUNE, 0) > 0) {
                    tooltip.add(I18n.format(Enchantments.FORTUNE.getName()));
                } else if (enchantments.getOrDefault(Enchantments.SILK_TOUCH, 0) > 0) {
                    tooltip.add(I18n.format(Enchantments.SILK_TOUCH.getName()));
                }
            }
        }
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        subItems.add((new ItemStack(itemIn, 1, 0)));
        subItems.add(getEditorStack());
        subItems.add((new ItemStack(itemIn, 1, 2)));
        if ((Boolean) Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", Boolean.FALSE) && Config.content().debug()) {
            ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.addEnchantment(Enchantments.EFFICIENCY, 5);
            stack.addEnchantment(Enchantments.UNBREAKING, 3);
            {
                ItemStack stack1 = stack.copy();
                stack1.addEnchantment(Enchantments.FORTUNE, 3);
                subItems.add(stack1);
            }
            {
                ItemStack stack1 = stack.copy();
                stack1.addEnchantment(Enchantments.SILK_TOUCH, 1);
                subItems.add(stack1);
            }
        }
    }

    @Override
    public boolean canMove(final ItemStack is, Enchantment enchantment) {
        if (is.getItemDamage() != 1)
            return false;
        final NBTTagList l = is.getEnchantmentTagList();
        return (l == null || l.tagCount() == 0) && (enchantment == Enchantments.SILK_TOUCH || enchantment == Enchantments.FORTUNE);
    }

    public ItemStack getEditorStack() {
        ItemStack stack = new ItemStack(this, 1, 1);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("HideFlags", 1);
        stack.setTagCompound(compound);
        return stack;
    }
}
