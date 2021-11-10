package com.yogpc.qp.recipe;

import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class QuarryBedrockModuleRecipe extends ShapelessRecipe {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "quarry_bedrock_recipe");
    public static final RecipeSerializer<QuarryBedrockModuleRecipe> SERIALIZER = new QuarryBedrockModuleSerializer();
    private final QPBlock targetBlock;

    public QuarryBedrockModuleRecipe(ResourceLocation id, QPBlock targetBlock) {
        super(id, QuarryPlus.modID + ":bedrock_module_recipe", makeOutputStack(targetBlock),
            NonNullList.of(Ingredient.of(), Ingredient.of(targetBlock), Ingredient.of(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE)));
        this.targetBlock = targetBlock;
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world) {
        var stacks = IntStream.range(0, inventory.getContainerSize())
            .mapToObj(inventory::getItem)
            .filter(Predicate.not(ItemStack::isEmpty))
            .toList();
        if (stacks.size() == 2) {
            var quarryStack = stacks.stream().filter(s -> s.getItem() == targetBlock.blockItem).findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
            var subNbt = quarryStack.getTagElement(BlockItem.BLOCK_ENTITY_TAG);
            var hasQuarry = !quarryStack.isEmpty() && (subNbt == null || !subNbt.getBoolean("bedrockRemove"));
            var hasModule = stacks.stream().map(ItemStack::getItem).anyMatch(Predicate.isEqual(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE));
            return hasQuarry && hasModule;
        }
        return false;
    }

    private static ItemStack makeOutputStack(Block targetBlock) {
        var quarryStack = new ItemStack(targetBlock);
        quarryStack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG).putBoolean("bedrockRemove", true);
        return quarryStack;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory) {
        var stacks = IntStream.range(0, inventory.getContainerSize())
            .mapToObj(inventory::getItem)
            .filter(Predicate.not(ItemStack::isEmpty))
            .toList();
        var quarryStack = stacks.stream().filter(s -> s.getItem() == targetBlock.blockItem).findFirst().map(ItemStack::copy).orElseThrow();
        quarryStack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG).putBoolean("bedrockRemove", true);
        return quarryStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static final class QuarryBedrockModuleSerializer implements RecipeSerializer<QuarryBedrockModuleRecipe> {
        @Override
        public QuarryBedrockModuleRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            var name = GsonHelper.getAsString(jsonObject, "target");
            Block block = Registry.BLOCK.get(new ResourceLocation(name));
            if (block instanceof QPBlock qpBlock) {
                return new QuarryBedrockModuleRecipe(resourceLocation, qpBlock);
            }
            throw new IllegalArgumentException("Invalid block %s(%s)".formatted(block, name));
        }

        @Override
        public QuarryBedrockModuleRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            var name = buf.readResourceLocation();
            Block block = Registry.BLOCK.get(name);
            if (block instanceof QPBlock qpBlock) {
                return new QuarryBedrockModuleRecipe(resourceLocation, qpBlock);
            }
            throw new IllegalArgumentException("Invalid block %s(%s)".formatted(block, name));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, QuarryBedrockModuleRecipe recipe) {
            buf.writeResourceLocation(recipe.targetBlock.getRegistryName());
        }
    }
}
