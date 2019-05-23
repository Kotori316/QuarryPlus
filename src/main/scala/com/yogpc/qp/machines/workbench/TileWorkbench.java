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
package com.yogpc.qp.machines.workbench;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.EnergyUsage;
import com.yogpc.qp.machines.base.HasInv;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import scala.Symbol;

//@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.tileentity.IInventoryConnection", modid = QuarryPlus.Optionals.COFH_modID)
public class TileWorkbench extends APowerTile implements HasInv, IDebugSender, IInteractionObject {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.workbench;
    public static final Symbol SYMBOL = Symbol.apply("WorkbenchPlus");
    public final NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    public final NonNullList<ItemStack> inventory2 = NonNullList.withSize(18, ItemStack.EMPTY);
    public List<WorkbenchRecipes> recipesList = Collections.emptyList();
    private WorkbenchRecipes currentRecipe = WorkbenchRecipes.dummyRecipe();
    private ItemHandler itemHandler = new ItemHandler();
    public boolean workContinue;

    public TileWorkbench() {
        super(Holder.workbenchTileType());
    }

    @Override
    public void tick() {
        super.tick();
        if (world != null && !world.isRemote) {
            if (isWorking()) {
                if (currentRecipe.microEnergy() <= getStoredEnergy() || Config.common().noEnergy().get()) {
                    useEnergy(currentRecipe.microEnergy(), currentRecipe.microEnergy(), true, EnergyUsage.WORKBENCH);
                    ItemStack stack = currentRecipe.getOutput();
                    ItemStack inserted = InvUtils.injectToNearTile(world, getPos(), stack);
                    if (!inserted.isEmpty()) {
                        InventoryHelper.spawnItemStack(world, getPos().getX(), getPos().getY(), getPos().getZ(), stack);
                    }
                    currentRecipe.inputsJ().forEach(inputList -> {
                        for (IngredientWithCount i : inputList) {
                            if (inventory.stream().anyMatch(i::shrink)) break;
                        }
                    });
                    for (int i = 0; i < inventory.size(); i++) {
                        if (inventory.get(i).isEmpty())
                            inventory.set(i, ItemStack.EMPTY);
                    }
                    markDirty();
                    setCurrentRecipeIndex(workContinue ? getRecipeIndex() : -1);
                }
            }
        }
    }

    @Override
    public boolean isWorking() {
        return enabled() && currentRecipe.hasContent();
    }

    @Override
    public void read(NBTTagCompound nbt) {
        super.read(nbt);
        NBTTagList list = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
        list.stream().map(b -> (NBTTagCompound) b).forEach(nbtTagCompound -> {
            int j = nbtTagCompound.getByte("Slot") & 255;
            ItemStack stack = ItemStack.read(nbtTagCompound);
            stack.setCount(nbtTagCompound.getInt("Count"));
            inventory.set(j, stack);
        });
        markDirty();
    }

    @Override
    public NBTTagCompound write(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.putByte("Slot", (byte) i);
            stack.write(nbttagcompound);
            nbttagcompound.remove("Count");
            nbttagcompound.putInt("Count", stack.getCount());
            list.add(nbttagcompound);
        }
        nbt.put("Items", list);
        return super.write(nbt);
    }

    @Override
    public int getSizeInventory() {
        return inventory.size() + inventory2.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (inventory.size() <= index && index < getSizeInventory()) {
            return inventory2.get(index - inventory.size());
        }
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (inventory.size() <= index && index < getSizeInventory())
            return ItemStackHelper.getAndSplit(inventory2, index - inventory.size(), count);
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (inventory.size() <= index && index < getSizeInventory())
            return ItemStackHelper.getAndRemove(inventory2, index - inventory.size());
        return ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (inventory.size() <= index && index < getSizeInventory()) {
            inventory2.set(index - inventory.size(), stack);
        } else
            inventory.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(getPos()) == this && player.getDistanceSqToCenter(getPos()) <= 64;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        recipesList = WorkbenchRecipes.getRecipe(inventory);
        inventory2.clear();
        for (int i = 0; i < recipesList.size(); i++) {
            setInventorySlotContents(inventory.size() + i, recipesList.get(i).getOutput());
        }
        if (getRecipeIndex() == -1) {
            setCurrentRecipeIndex(-1);
            finishWork();
        } else {
            startWork();
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0:
                return getRecipeIndex();
            case 1:
                return 0;//(int) getStoredEnergy();
            case 2:
                return workContinue ? 1 : 0;
        }
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0:
                setCurrentRecipeIndex(value);
                break;
            case 1:
//                setStoredEnergy(value);
                break;
            case 2:
                workContinue = value == 1;
                break;
        }
    }

    @Override
    public int getFieldCount() {
        return 3;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public List<TextComponentString> getDebugMessages() {
        return Arrays.asList(new TextComponentString(currentRecipe.toString()),
            new TextComponentString("Work mode : " + (workContinue ? "Continue" : "Only once")));
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.workbench;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> itemHandler));
        }
        return super.getCapability(cap, side);
    }

    public void setCurrentRecipeIndex(int recipeIndex) {
        if (recipeIndex >= 0 && recipesList.size() > recipeIndex) {
            this.currentRecipe = recipesList.get(recipeIndex);
        } else {
            this.currentRecipe = WorkbenchRecipes.dummyRecipe();
        }
        configure(Config.common().workbenchMaxReceive().get() * APowerTile.MicroJtoMJ, currentRecipe.microEnergy());
    }

    @OnlyIn(Dist.CLIENT)
    public int getProgressScaled(int scale) {
        if (isWorking())
            return (int) (getStoredEnergy() * scale / currentRecipe.microEnergy());
        else
            return 0;
    }

    public int getRecipeIndex() {
        return recipesList.indexOf(currentRecipe);
    }

//    @Override
//    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
//    public ConnectionType canConnectInventory(EnumFacing from) {
//        return ConnectionType.FORCE;
//    }

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public ITextComponent getName() {
        return new TextComponentTranslation(getDebugName());
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerWorkbench(playerIn, this);
    }

    @Override
    public String getGuiID() {
        return GUI_ID;
    }

    private class ItemHandler implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            setInventorySlotContents(slot, stack);
        }

        @Override
        public int getSlots() {
            return inventory.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return TileWorkbench.this.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty())
                return ItemStack.EMPTY;
            ItemStack inSlot = getStackInSlot(slot).copy();
            if (!inSlot.isEmpty()) {
                if (ItemHandlerHelper.canItemStacksStack(inSlot, stack)) {
                    if (!simulate) {
                        inSlot.grow(stack.getCount());
                        setStackInSlot(slot, inSlot);
                        markDirty();
                    }
                    return ItemStack.EMPTY;
                } else {
                    return stack;
                }
            } else {
                if (!simulate) {
                    setStackInSlot(slot, stack.copy());
                    markDirty();
                }
                return ItemStack.EMPTY;
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return getInventoryStackLimit();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isItemValidForSlot(slot, stack);
        }
    }
}
