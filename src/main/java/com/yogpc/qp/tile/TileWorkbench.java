package com.yogpc.qp.tile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cofh.api.tileentity.IInventoryConnection;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.inventory.IInventoryConnection", modid = QuarryPlus.Optionals.COFH_modID)
public class TileWorkbench extends APowerTile implements IInventory, IInventoryConnection {
    public final NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    public final NonNullList<ItemStack> inventory2 = NonNullList.withSize(18, ItemStack.EMPTY);
    public List<WorkbenchRecipes> recipesList = Collections.emptyList();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Optional<WorkbenchRecipes> currentRecipe = Optional.empty();

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (currentRecipe.isPresent()) {
                WorkbenchRecipes recipes = currentRecipe.get();
                if (recipes.energy() <= getStoredEnergy()) {
                    useEnergy(recipes.energy(), recipes.energy(), true);
                    ItemStack stack = recipes.output().toStack(1);
                    ItemStack inserted = InvUtils.injectToNearTile(getWorld(), getPos(), stack);
                    if (!inserted.isEmpty()) {
                        InventoryHelper.spawnItemStack(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), stack);
                    }
                    recipes.inputsJ().forEach(v1 ->
                            inventory.stream().filter(v1::isItemEqual).findFirst().ifPresent(stack1 -> stack1.shrink(v1.getCount()))
                    );
                    setCurrentRecipe(-1);
                    markDirty();
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
        ItemStackHelper.loadAllItems(nbttc, inventory);
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttc) {
        ItemStackHelper.saveAllItems(nbttc, inventory);
        return super.writeToNBT(nbttc);
    }

    @Override
    public ConnectionType canConnectInventory(EnumFacing from) {
        return ConnectionType.FORCE;
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
        if (27 <= index && index < 45) {
            return inventory2.get(index - 27);
        }
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (27 <= index && index < 45)
            return ItemStackHelper.getAndSplit(inventory, index - 27, count);
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
        for (int i = 0; i < recipesList.size(); i++) {
            setInventorySlotContents(27 + i, recipesList.get(i).output().toStack(1));
        }
        if (getRecipeIndex() == -1) {
            setCurrentRecipe(-1);
        }
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public String getName() {
        return "tile.workbenchplus.name";
    }

    @Override
    public boolean hasCustomName() {
        return false;
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
        return currentRecipe.map(workbenchRecipes -> recipesList.indexOf(workbenchRecipes)).orElse(-1);
    }
}
