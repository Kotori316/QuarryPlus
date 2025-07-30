package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.stream.IntStream;

public final class FilterModuleExpandRecipe extends ShapedRecipe {
    static final int MAX_ROWS = 6;
    static final int ROW_INCREMENT = 2;

    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "filter_module_expand_recipe");
    public static final RecipeSerializer<FilterModuleExpandRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(FilterModuleExpandRecipe::new);

    private final Item moduleItem;

    public FilterModuleExpandRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, "", pCategory, 3, 3, createIngredients(Holder.ITEM_FILTER_MODULE), new ItemStack(Holder.ITEM_FILTER_MODULE));
        this.moduleItem = Holder.ITEM_FILTER_MODULE;
    }

    /**
     * Only for test
     */
    FilterModuleExpandRecipe(ResourceLocation pId, CraftingBookCategory pCategory, Item moduleItem) {
        super(pId, "", pCategory, 3, 3, createIngredients(moduleItem), new ItemStack(Holder.ITEM_FILTER_MODULE));
        this.moduleItem = moduleItem;
    }

    static NonNullList<Ingredient> createIngredients(Item moduleItem) {
        var chest = Ingredient.of(Items.CHEST);
        var module = Ingredient.of(moduleItem);
        return NonNullList.of(
            Ingredient.EMPTY,
            chest, chest, chest,
            chest, module, chest,
            chest, chest, chest
        );
    }

    @Override
    public boolean matches(CraftingContainer pInv, Level pLevel) {
        if (!super.matches(pInv, pLevel)) {
            return false;
        }
        var module = findModule(pInv);
        if (module.isEmpty()) {
            return false;
        }
        var rows = FilterModuleItem.getRowsFromStack(module);
        return rows + ROW_INCREMENT <= MAX_ROWS;
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
        var module = findModule(pContainer).copy();
        var rows = FilterModuleItem.getRowsFromStack(module);
        module.getOrCreateTag().putInt(FilterModuleItem.KEY_ITEM_ROWS, rows + ROW_INCREMENT);
        return module;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    ItemStack findModule(CraftingContainer container) {
        return IntStream.range(0, container.getContainerSize())
            .mapToObj(container::getItem)
            .filter(i -> i.is(moduleItem))
            .findAny()
            .orElse(ItemStack.EMPTY);
    }
}
