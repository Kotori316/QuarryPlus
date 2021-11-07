package com.yogpc.qp.recipe;

import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;

public class QuarryBedrockModuleRecipe extends ShapelessRecipe {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "quarry_bedrock_recipe");
    public static final SimpleRecipeSerializer<QuarryBedrockModuleRecipe> SERIALIZER = new SimpleRecipeSerializer<>(QuarryBedrockModuleRecipe::new);

    public QuarryBedrockModuleRecipe(ResourceLocation id) {
        super(id, "", makeOutputStack(),
            NonNullList.of(Ingredient.of(), Ingredient.of(QuarryPlus.ModObjects.BLOCK_QUARRY), Ingredient.of(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE)));
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world) {
        var stacks = IntStream.range(0, inventory.getContainerSize())
            .mapToObj(inventory::getItem)
            .filter(Predicate.not(ItemStack::isEmpty))
            .toList();
        if (stacks.size() == 2) {
            var quarryStack = stacks.stream().filter(s -> s.getItem() == QuarryPlus.ModObjects.BLOCK_QUARRY.blockItem).findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
            var subNbt = quarryStack.getTagElement(BlockItem.BLOCK_ENTITY_TAG);
            var hasQuarry = !quarryStack.isEmpty() && (subNbt == null || !subNbt.getBoolean("bedrockRemove"));
            var hasModule = stacks.stream().map(ItemStack::getItem).anyMatch(Predicate.isEqual(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE));
            return hasQuarry && hasModule;
        }
        return false;
    }

    private static ItemStack makeOutputStack() {
        var quarryStack = new ItemStack(QuarryPlus.ModObjects.BLOCK_QUARRY);
        quarryStack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG).putBoolean("bedrockRemove", true);
        return quarryStack;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory) {
        var stacks = IntStream.range(0, inventory.getContainerSize())
            .mapToObj(inventory::getItem)
            .filter(Predicate.not(ItemStack::isEmpty))
            .toList();
        var quarryStack = stacks.stream().filter(s -> s.getItem() == QuarryPlus.ModObjects.BLOCK_QUARRY.blockItem).findFirst().map(ItemStack::copy).orElseThrow();
        quarryStack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG).putBoolean("bedrockRemove", true);
        return quarryStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
