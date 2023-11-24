package com.yogpc.qp.machines.workbench;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

class DummyRecipe extends WorkbenchRecipe {
    static final DummyRecipe INSTANCE = new DummyRecipe();
    public static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "builtin_dummy");

    private DummyRecipe() {
        super(ItemStack.EMPTY, 0, false);
    }

    @Override
    public List<IngredientList> inputs() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasContent() {
        return false;
    }

    @Override
    protected String getSubTypeName() {
        return "dummy";
    }

    @Override
    protected ItemStack getOutput(List<ItemStack> inventory, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public String toString() {
        return "WorkbenchRecipe NoRecipe";
    }
}
