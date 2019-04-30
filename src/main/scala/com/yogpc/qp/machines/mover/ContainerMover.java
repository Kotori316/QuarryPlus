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

package com.yogpc.qp.machines.mover;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.machines.base.IEnchantableItem;
import com.yogpc.qp.machines.base.SlotMover;
import com.yogpc.qp.utils.Holder;
import com.yogpc.qp.utils.LoopList;
import jp.t2v.lab.syntax.MapStreamSyntax;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerMover extends Container {
    public final IInventory craftMatrix = new InventoryBasic(new TextComponentString("Matrix"), 2) {
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

    public ContainerMover(final IInventory player, final World w, BlockPos pos) {
        this.worldObj = w;
        this.pos = pos;
        int row;
        int col;
        for (col = 0; col < 2; ++col)
            addSlot(new SlotMover(this.craftMatrix, col, 8 + col * 144, 35));

        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlot(new Slot(player, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        for (col = 0; col < 9; ++col)
            addSlot(new Slot(player, col, 8 + col * 18, 142));
    }

    @Override
    public void onContainerClosed(final EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!worldObj.isRemote) {
            IntStream.range(0, craftMatrix.getSizeInventory())
                .mapToObj(craftMatrix::removeStackFromSlot)
                .filter(MapStreamSyntax.not(ItemStack::isEmpty))
                .forEach(s -> playerIn.dropItem(s, false));
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        return this.worldObj.getBlockState(pos).getBlock() == Holder.blockMover() && playerIn.getDistanceSqToCenter(pos) <= 64.0D;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        ItemStack pickaxe = craftMatrix.getStackInSlot(0);
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(pickaxe);
        ItemStack enchTile = craftMatrix.getStackInSlot(1);
        if (enchantments.isEmpty() || enchTile.isEmpty()) {
            avail = 0;
            list.setList(Collections.emptyList());
        } else {
            int previousSize = list.size();
            if (!enchTile.isEmpty() && enchTile.getItem() instanceof IEnchantableItem) {
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
            listener.sendWindowProperty(this, 0, this.avail);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, this.avail);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateProgressBar(final int i, final int data) {
        this.avail = data;
    }

    public void setAvail(D d) {
        avail += d.offset;
        if (!worldObj.isRemote) {
            for (IContainerListener listener : this.listeners)
                listener.sendWindowProperty(this, 0, this.avail);
        }
    }

    public Optional<Enchantment> getEnchantment() {
        return list.getOptional(avail).map(tuple -> tuple.enchantment);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < 2) {
                if (!mergeItemStack(remain, 2, 38, true))
                    return ItemStack.EMPTY;
            } else {
                Slot toSlot;
                final ItemStack put = ItemHandlerHelper.copyStackWithSize(remain, 1);
                boolean changed = false;
                toSlot = this.inventorySlots.get(0);
                if (/*!changed(true) &&*/ toSlot.isItemValid(remain) && toSlot.getStack().isEmpty()) {
                    toSlot.putStack(put);
                    remain.shrink(1);
                    changed = true;
                }
                toSlot = this.inventorySlots.get(1);
                if (!changed && toSlot.isItemValid(remain) && toSlot.getStack().isEmpty()) {
                    toSlot.putStack(put);
                    remain.shrink(1);
                    changed = true;
                }
                if (!changed)
                    return ItemStack.EMPTY;
            }
            if (remain.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
            if (remain.getCount() == src.getCount())
                return ItemStack.EMPTY;
            slot.onTake(playerIn, remain);
        }
        return src;
    }

    public void moveEnchant() {
        Tuple tuple = list.get(avail);
        ItemStack tileItem = craftMatrix.getStackInSlot(1);
        if (tuple == null || tileItem.isEmpty() || !((IEnchantableItem) tileItem.getItem()).canMove(tileItem, tuple.enchantment)) {
            return;
        }
        NBTTagList list = tileItem.getEnchantmentTagList();
        if (/*list == null ||*/ EnchantmentHelper.getEnchantmentLevel(tuple.enchantment, tileItem) == 0) {
            //add new enchantment (Level 1)
            tileItem.addEnchantment(tuple.enchantment, 1);
            if (tuple.level == 1) {
                this.list.remove(avail);
            } else {
                this.list.set(avail, tuple.levelDown());
            }
            downLevel(tuple.enchantment, craftMatrix.getStackInSlot(0));
        } else {
            for (int i = 0; i < list.size(); i++) {
                NBTTagCompound nbt = list.getCompound(i);
                if (ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(nbt.getString("id"))) == tuple.enchantment) {
                    short l = nbt.getShort("lvl");
                    if (l < tuple.enchantment.getMaxLevel()) {
                        nbt.setShort("lvl", (short) (l + 1));
                        if (tuple.level == 1) {
                            this.list.remove(avail);
                        } else {
                            this.list.set(avail, tuple.levelDown());
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
            for (int i = 0; i < list.size(); i++) {
                NBTTagCompound nbt = list.getCompound(i);
                if (ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(nbt.getString("id"))) == enchantment) {
                    short l = nbt.getShort("lvl");
                    if (l == 1) {
                        list.removeTag(i);
                    } else {
                        nbt.setShort("lvl", (short) (l - 1));
                    }
                    break;
                }
            }
            if (list.isEmpty()) {
                NBTTagCompound compound = stack.getTag();
                if (compound != null) {
                    compound.removeTag("ench");
                    if (compound.isEmpty())
                        stack.setTag(null);
                }
            }
        }
    }

    public enum D {
        UP(-1), DOWN(+1);
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
            if (obj instanceof Tuple) {
                Tuple tuple = (Tuple) obj;
                return tuple.enchantment == this.enchantment && tuple.level == this.level;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return enchantment.hashCode() ^ level;
        }

        public Tuple levelDown() {
            return cloneWithLevel(level - 1);
        }

        public Tuple cloneWithLevel(int newLevel) {
            return new Tuple(enchantment, newLevel);
        }

        @Override
        public String toString() {
            return "Tuple{" +
                "enchantment=" + enchantment.getRegistryName() +
                ", level=" + level +
                '}';
        }
    }
}
