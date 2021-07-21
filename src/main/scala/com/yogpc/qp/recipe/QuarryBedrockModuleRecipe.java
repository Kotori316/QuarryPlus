package com.yogpc.qp.recipe;

import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class QuarryBedrockModuleRecipe extends ShapelessRecipe {
    public static final Identifier NAME = new Identifier(QuarryPlus.modID, "quarry_bedrock_recipe");
    public static final SpecialRecipeSerializer<QuarryBedrockModuleRecipe> SERIALIZER = new SpecialRecipeSerializer<>(QuarryBedrockModuleRecipe::new);

    public QuarryBedrockModuleRecipe(Identifier id) {
        super(id, "", makeOutputStack(),
            DefaultedList.copyOf(Ingredient.empty(), Ingredient.ofItems(QuarryPlus.ModObjects.BLOCK_QUARRY), Ingredient.ofItems(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE)));
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        var stacks = IntStream.range(0, inventory.size())
            .mapToObj(inventory::getStack)
            .filter(Predicate.not(ItemStack::isEmpty))
            .toList();
        if (stacks.size() == 2) {
            var quarryStack = stacks.stream().filter(s -> s.getItem() == QuarryPlus.ModObjects.BLOCK_QUARRY.blockItem).findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
            var subNbt = quarryStack.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
            var hasQuarry = !quarryStack.isEmpty() && (subNbt == null || !subNbt.getBoolean("bedrockRemove"));
            var hasModule = stacks.stream().map(ItemStack::getItem).anyMatch(Predicate.isEqual(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE));
            return hasQuarry && hasModule;
        }
        return false;
    }

    private static ItemStack makeOutputStack() {
        var quarryStack = new ItemStack(QuarryPlus.ModObjects.BLOCK_QUARRY);
        quarryStack.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY).putBoolean("bedrockRemove", true);
        return quarryStack;
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        var stacks = IntStream.range(0, inventory.size())
            .mapToObj(inventory::getStack)
            .filter(Predicate.not(ItemStack::isEmpty))
            .toList();
        var quarryStack = stacks.stream().filter(s -> s.getItem() == QuarryPlus.ModObjects.BLOCK_QUARRY.blockItem).findFirst().map(ItemStack::copy).orElseThrow();
        quarryStack.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY).putBoolean("bedrockRemove", true);
        return quarryStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
