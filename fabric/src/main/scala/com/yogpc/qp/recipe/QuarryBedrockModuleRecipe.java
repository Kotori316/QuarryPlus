package com.yogpc.qp.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Predicate;
import java.util.stream.IntStream;

public class QuarryBedrockModuleRecipe extends ShapelessRecipe {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "quarry_bedrock_recipe");
    public static final RecipeSerializer<QuarryBedrockModuleRecipe> SERIALIZER = new QuarryBedrockModuleSerializer();
    private final QPBlock targetBlock;

    public QuarryBedrockModuleRecipe(QPBlock targetBlock) {
        super(QuarryPlus.modID + ":bedrock_module_recipe", CraftingBookCategory.MISC, makeOutputStack(targetBlock),
            NonNullList.of(Ingredient.of(), Ingredient.of(targetBlock), Ingredient.of(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE)));
        this.targetBlock = targetBlock;
    }

    QuarryBedrockModuleRecipe(ResourceLocation targetBlockId) {
        this(fromId(targetBlockId));
    }

    private static QPBlock fromId(ResourceLocation blockId) {
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block instanceof QPBlock qpBlock) {
            return qpBlock;
        }
        throw new IllegalArgumentException("Invalid block %s(%s)".formatted(block, blockId));
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world) {
        var stacks = IntStream.range(0, inventory.getContainerSize())
            .mapToObj(inventory::getItem)
            .filter(Predicate.not(ItemStack::isEmpty))
            .toList();
        if (stacks.size() == 2) {
            var quarryStack = stacks.stream().filter(s -> s.getItem() == targetBlock.blockItem).findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
            var subNbt = BlockItem.getBlockEntityData(quarryStack);
            var hasQuarry = !quarryStack.isEmpty() && (subNbt == null || !subNbt.getBoolean("bedrockRemove"));
            var hasModule = stacks.stream().map(ItemStack::getItem).anyMatch(Predicate.isEqual(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE));
            return hasQuarry && hasModule;
        }
        return false;
    }

    private static ItemStack makeOutputStack(Block targetBlock) {
        var quarryStack = new ItemStack(targetBlock);
        quarryStack.getOrCreateTagElement("BlockEntityTag").putBoolean("bedrockRemove", true);
        return quarryStack;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess access) {
        var stacks = IntStream.range(0, inventory.getContainerSize())
            .mapToObj(inventory::getItem)
            .filter(Predicate.not(ItemStack::isEmpty))
            .toList();
        var quarryStack = stacks.stream().filter(s -> s.getItem() == targetBlock.blockItem).findFirst().map(ItemStack::copy).orElseThrow();
        quarryStack.getOrCreateTagElement("BlockEntityTag").putBoolean("bedrockRemove", true);
        return quarryStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static final class QuarryBedrockModuleSerializer implements RecipeSerializer<QuarryBedrockModuleRecipe> {
        static final Codec<QuarryBedrockModuleRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("target").forGetter(t -> t.targetBlock.getRegistryName())
            ).apply(instance, QuarryBedrockModuleRecipe::new));

        @Override
        public Codec<QuarryBedrockModuleRecipe> codec() {
            return CODEC;
        }

        @Override
        public QuarryBedrockModuleRecipe fromNetwork(FriendlyByteBuf buf) {
            var name = buf.readResourceLocation();
            Block block = BuiltInRegistries.BLOCK.get(name);
            if (block instanceof QPBlock qpBlock) {
                return new QuarryBedrockModuleRecipe(qpBlock);
            }
            throw new IllegalArgumentException("Invalid block %s(%s)".formatted(block, name));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, QuarryBedrockModuleRecipe recipe) {
            buf.writeResourceLocation(recipe.targetBlock.getRegistryName());
        }
    }
}
