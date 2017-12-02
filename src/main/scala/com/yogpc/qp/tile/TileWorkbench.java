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
package com.yogpc.qp.tile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import cofh.api.tileentity.IInventoryConnection;
import com.yogpc.qp.Config;
import com.yogpc.qp.NonNullList;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.tileentity.IInventoryConnection", modid = QuarryPlus.Optionals.COFH_tileentity)
public class TileWorkbench extends APowerTile implements HasInv, IDebugSender, IInventoryConnection {
    public final NonNullList<ItemStack> inventory = NonNullList.withSize(27, com.yogpc.qp.version.VersionUtil.empty());
    public final NonNullList<ItemStack> inventory2 = NonNullList.withSize(18, com.yogpc.qp.version.VersionUtil.empty());
    public List<WorkbenchRecipes> recipesList = Collections.emptyList();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Optional<WorkbenchRecipes> currentRecipe = Optional.empty();
    public boolean workcontinue;

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (currentRecipe.isPresent()) {
                WorkbenchRecipes recipes = currentRecipe.get();
                if (recipes.energy() <= getStoredEnergy() || Config.content().noEnergy()) {
                    useEnergy(recipes.energy(), recipes.energy(), true);
                    ItemStack stack = recipes.output().toStack(1);
                    ItemStack inserted = InvUtils.injectToNearTile(getWorld(), getPos(), stack);
                    if (VersionUtil.nonEmpty(inserted)) {
                        InventoryHelper.spawnItemStack(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), stack);
                    }
                    recipes.inputsJ().forEach(v1 ->
                            inventory.stream().filter(v1::isItemEqual).findFirst().ifPresent(stack1 -> VersionUtil.shrink(stack1, VersionUtil.getCount(v1)))
                    );
                    markDirty();
                    setCurrentRecipe(workcontinue ? getRecipeIndex() : -1);
                }
            }
        }
    }

    @Override
    protected boolean isWorking() {
        return currentRecipe.isPresent();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        NBTTagList list = nbttc.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, list.tagCount()).mapToObj(list::getCompoundTagAt).forEach(nbtTagCompound -> {
            int j = nbtTagCompound.getByte("Slot") & 255;
            ItemStack stack = VersionUtil.fromNBTTag(nbtTagCompound);
            VersionUtil.setCount(stack, nbtTagCompound.getInteger("Count"));
            inventory.set(j, stack);
        });
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttc) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (VersionUtil.nonEmpty(stack)) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                stack.writeToNBT(nbttagcompound);
                nbttagcompound.removeTag("Count");
                nbttagcompound.setInteger("Count", VersionUtil.getCount(stack));
                list.appendTag(nbttagcompound);
            }
        }
        nbttc.setTag("Items", list);
        return super.writeToNBT(nbttc);
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_tileentity)
    public ConnectionType canConnectInventory(EnumFacing from) {
        return ConnectionType.FORCE;
    }

    @Override
    public int getSizeInventory() {
        return inventory.size() + inventory2.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(VersionUtil::isEmpty);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (27 <= index && index < 45) {
            return inventory2.get(index - 27);
        }
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (27 <= index && index < 45)
            return ItemStackHelper.getAndSplit(inventory2, index - 27, count);
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (27 <= index && index < 45)
            return ItemStackHelper.getAndRemove(inventory2, index - 27);
        return ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (27 <= index && index < 45) {
            inventory2.set(index - 27, stack);
        } else
            inventory.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return getWorld().getTileEntity(getPos()) == this && player.getDistanceSqToCenter(getPos()) <= 64;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        recipesList = WorkbenchRecipes.getRecipe(inventory);
        inventory2.clear();
        for (int i = 0; i < recipesList.size(); i++) {
            setInventorySlotContents(27 + i, recipesList.get(i).output().toStack(1));
        }
        if (getRecipeIndex() == -1) {
            setCurrentRecipe(-1);
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
                return (int) getStoredEnergy();
            case 2:
                return workcontinue ? 1 : 0;
        }
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0:
                setCurrentRecipe(value);
                break;
            case 1:
                setStoredEnergy(value);
                break;
            case 2:
                workcontinue = value == 1;
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
    public List<TextComponentString> getDebugmessages() {
        return Arrays.asList(new TextComponentString(currentRecipe.map(WorkbenchRecipes::toString).orElse("No recipe.")),
                new TextComponentString("Work mode : " + (workcontinue ? "Continue" : "Only once")));
    }

    @Override
    public String getName() {
        return "tile.workbenchplus.name";
    }

    public void setCurrentRecipe(int recipeIndex) {
        if (recipeIndex >= 0 && recipesList.size() > recipeIndex) {
            this.currentRecipe = Optional.of(recipesList.get(recipeIndex));
        } else {
            this.currentRecipe = Optional.empty();
        }
        configure(250, currentRecipe.map(WorkbenchRecipes::energy).orElse(0d));
    }

    @SideOnly(Side.CLIENT)
    public int getProgressScaled(int scale) {
        return currentRecipe.map(w -> getStoredEnergy() * scale / w.energy()).orElse(0d).intValue();
    }

    public int getRecipeIndex() {
        return currentRecipe.map(recipesList::indexOf).orElse(-1);
    }
}
