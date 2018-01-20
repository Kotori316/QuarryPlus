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

package com.yogpc.qp.container;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.item.IEnchantableItem;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import static com.yogpc.qp.version.VersionUtil.isEmpty;
import static com.yogpc.qp.version.VersionUtil.nonEmpty;

public class ContainerMover extends Container {
    public IInventory craftMatrix = new InventoryBasic("Matrix", false, 2) {
        @Override
        public void markDirty() {
            super.markDirty();
            detectAndSendChanges();
        }
    };
    private final World worldObj;
    private final BlockPos pos;
    private final LoopList<Tuple> list = new LoopList<>();
    private int avail = 0;

    public ContainerMover(final IInventory player, final World w, final int x, final int y, final int z) {
        this.worldObj = w;
        pos = new BlockPos(x, y, z);
        int row;
        int col;
        for (col = 0; col < 2; ++col)
            addSlotToContainer(new SlotMover(this.craftMatrix, col, 8 + col * 144, 35));

        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlotToContainer(new Slot(player, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        for (col = 0; col < 9; ++col)
            addSlotToContainer(new Slot(player, col, 8 + col * 18, 142));
    }

    @Override
    public void onContainerClosed(final EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!worldObj.isRemote) {
            for (int var2 = 0; var2 < 2; ++var2) {
                final ItemStack var3 = this.craftMatrix.removeStackFromSlot(var2);
                if (nonEmpty(var3))
                    playerIn.dropItem(var3, false);
            }
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        return this.worldObj.getBlockState(pos).getBlock() == QuarryPlusI.blockMover() && playerIn.getDistanceSqToCenter(pos) <= 64.0D;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        ItemStack pickaxe = craftMatrix.getStackInSlot(0);
        Map<Enchantment, Integer> enchantments = pickaxe == null ? Collections.emptyMap() : EnchantmentHelper.getEnchantments(pickaxe);
        ItemStack enchTile = craftMatrix.getStackInSlot(1);
        if (enchantments.isEmpty() || isEmpty(enchTile)) {
            avail = 0;
            list.setList(Collections.emptyList());
        } else {
            int previousSize = list.size();
            if (nonEmpty(enchTile) && enchTile.getItem() instanceof IEnchantableItem) {
                IEnchantableItem item = (IEnchantableItem) enchTile.getItem();
                list.setList(enchantments.entrySet().stream().map(Tuple::new)
                        .filter(tuple -> item.canMove(enchTile, tuple.enchantment) &&
                                EnchantmentHelper.getEnchantmentLevel(tuple.enchantment, enchTile) < tuple.enchantment.getMaxLevel())
                        .collect(Collectors.toCollection(LinkedList::new)));
            } else {
                list.setList(enchantments.entrySet().stream().map(Tuple::new).collect(Collectors.toCollection(LinkedList::new)));
            }
            if (avail % (previousSize == 0 ? 1 : previousSize) > list.size()) {
                avail = 0;
            }
        }
        for (IContainerListener listener : this.listeners)
            VersionUtil.sendWindowProperty(listener, this, 0, this.avail);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        VersionUtil.sendWindowProperty(listener, this, 0, this.avail);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(final int i, final int data) {
        this.avail = data;
    }

    public void setAvail(D d) {
        avail += d.offset;
        if (!worldObj.isRemote) {
            for (IContainerListener listener : this.listeners)
                VersionUtil.sendWindowProperty(listener, this, 0, this.avail);
        }
    }

    public Optional<Enchantment> getEnchantment() {
        return list.getOptional(avail).map(tuple -> tuple.enchantment);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        ItemStack src = com.yogpc.qp.version.VersionUtil.empty();
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < 2) {
                if (!mergeItemStack(remain, 2, 38, true))
                    return com.yogpc.qp.version.VersionUtil.empty();
            } else {
                Slot toslot;
                final ItemStack put = ItemHandlerHelper.copyStackWithSize(remain, 1);
                boolean changed = false;
                toslot = this.inventorySlots.get(0);
                if (/*!changed(true) &&*/ toslot.isItemValid(remain) && isEmpty(toslot.getStack())) {
                    toslot.putStack(put);
                    VersionUtil.shrink(remain, 1);
                    changed = true;
                }
                toslot = this.inventorySlots.get(1);
                if (!changed && toslot.isItemValid(remain) && isEmpty(toslot.getStack())) {
                    toslot.putStack(put);
                    VersionUtil.shrink(remain, 1);
                    changed = true;
                }
                if (!changed)
                    return com.yogpc.qp.version.VersionUtil.empty();
            }
            if (isEmpty(remain))
                slot.putStack(com.yogpc.qp.version.VersionUtil.empty());
            else
                slot.onSlotChanged();
            if (VersionUtil.getCount(remain) == VersionUtil.getCount(src))
                return com.yogpc.qp.version.VersionUtil.empty();
            VersionUtil.onTake(slot, playerIn, remain);
        }
        return src;
    }

    public void moveEnchant() {
        Tuple tuple = list.get(avail);
        ItemStack tileItem = craftMatrix.getStackInSlot(1);
        if (tuple == null || isEmpty(tileItem) || !((IEnchantableItem) tileItem.getItem()).canMove(tileItem, tuple.enchantment)) {
            return;
        }
        NBTTagList list = tileItem.getEnchantmentTagList();
        if (/*list == null ||*/ EnchantmentHelper.getEnchantmentLevel(tuple.enchantment, tileItem) == 0) {
            //add new enchantment (Level 1)
            tileItem.addEnchantment(tuple.enchantment, 1);
            if (tuple.level == 1) {
                this.list.remove(avail);
            } else {
                this.list.set(avail, tuple.leveldown());
            }
            downLevel(tuple.enchantment, craftMatrix.getStackInSlot(0));
        } else {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbt = list.getCompoundTagAt(i);
                if (Enchantment.getEnchantmentByID(nbt.getShort("id")) == tuple.enchantment) {
                    short l = nbt.getShort("lvl");
                    if (l < tuple.enchantment.getMaxLevel()) {
                        nbt.setShort("lvl", (short) (l + 1));
                        if (tuple.level == 1) {
                            this.list.remove(avail);
                        } else {
                            this.list.set(avail, tuple.leveldown());
                        }
                        downLevel(tuple.enchantment, craftMatrix.getStackInSlot(0));
                    }
                    return;
                }
            }
        }
    }

    private static void downLevel(Enchantment enchantment, ItemStack stack) {
        NBTTagList list = stack.getEnchantmentTagList();
        /*if (list != null) */
        {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbt = list.getCompoundTagAt(i);
                if (Enchantment.getEnchantmentByID(nbt.getShort("id")) == enchantment) {
                    short l = nbt.getShort("lvl");
                    if (l == 1) {
                        list.removeTag(i);
                    } else {
                        nbt.setShort("lvl", (short) (l - 1));
                    }
                    break;
                }
            }
            if (list.hasNoTags()) {
                NBTTagCompound compound = stack.getTagCompound();
                if (compound != null) {
                    compound.removeTag("ench");
                    if (compound.hasNoTags())
                        stack.setTagCompound(null);
                }
            }
        }
    }

    public enum D {
        UP(-1), DOUN(+1);
        public final int offset;

        D(int offset) {
            this.offset = offset;
        }
    }

    private static class Tuple {
        final Enchantment enchantment;
        final int level;

        public Tuple(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }

        public Tuple(Map.Entry<Enchantment, Integer> entry) {
            this(entry.getKey(), entry.getValue());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (Tuple.class.isInstance(obj)) {
                Tuple tuple = (Tuple) obj;
                return tuple.enchantment == this.enchantment && tuple.level == this.level;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return enchantment.hashCode() ^ level;
        }

        public Tuple leveldown() {
            return cloneWithLevel(level - 1);
        }

        public Tuple cloneWithLevel(int newLevel) {
            return new Tuple(enchantment, newLevel);
        }
    }
}
