package com.yogpc.qp.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.QpBlock;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Used only in fabric. Registered in all platforms.
 */
public final class InstallBedrockModuleRecipe extends ShapelessRecipe {
    public static final String NAME = "install_bedrock_module_recipe";
    public static final RecipeSerializer<InstallBedrockModuleRecipe> SERIALIZER = new Serializer();
    private final QpBlock block;

    public InstallBedrockModuleRecipe(QpBlock block) {
        super(QuarryPlus.modID + ":" + NAME, CraftingBookCategory.MISC, resultItem(block), getIngredients(block));
        this.block = block;
    }

    InstallBedrockModuleRecipe(ResourceLocation targetBlockId) {
        this(fromId(targetBlockId));
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level)) {
            return false;
        }
        // Check quarry's component
        var stack = input.items().stream().filter(s -> s.getItem() == block.blockItem).findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
        var installed = stack.getOrDefault(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, Boolean.FALSE);

        return !installed;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var stack = input.items().stream().filter(s -> s.getItem() == block.blockItem).findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
        stack.set(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, true);
        return stack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static QpBlock fromId(ResourceLocation blockId) {
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block instanceof QpBlock qpBlock) {
            return qpBlock;
        }
        throw new IllegalArgumentException("Invalid block %s(%s)".formatted(block, blockId));
    }

    ResourceLocation getTargetBlockId() {
        return block.name;
    }

    private static @NotNull NonNullList<Ingredient> getIngredients(QpBlock block) {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(block), Ingredient.of(PlatformAccess.getAccess().registerObjects().bedrockModuleItem().get()));
    }

    static ItemStack resultItem(QpBlock block) {
        var stack = new ItemStack(block);
        stack.set(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, true);
        return stack;
    }

    private static final class Serializer implements RecipeSerializer<InstallBedrockModuleRecipe> {
        public static final MapCodec<InstallBedrockModuleRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecordCodecBuilder.of(InstallBedrockModuleRecipe::getTargetBlockId, "target", ResourceLocation.CODEC)
        ).apply(i, InstallBedrockModuleRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, InstallBedrockModuleRecipe> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(InstallBedrockModuleRecipe::new, InstallBedrockModuleRecipe::getTargetBlockId).cast();

        @Override
        public MapCodec<InstallBedrockModuleRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, InstallBedrockModuleRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static Builder builder(QpBlock block) {
        return new Builder(block);
    }

    public static final class Builder implements RecipeBuilder {
        private final QpBlock block;
        private final RecipeCategory category = RecipeCategory.MISC;
        private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

        public Builder(QpBlock block) {
            this.block = block;
        }

        @Override
        public Builder unlockedBy(String name, Criterion<?> criterion) {
            criteria.put(name, criterion);
            return this;
        }

        @Override
        public Builder group(@Nullable String groupName) {
            throw new UnsupportedOperationException("Group definition is not supported");
        }

        @Override
        public Item getResult() {
            return block.blockItem;
        }

        @Override
        public void save(RecipeOutput recipeOutput, ResourceLocation id) {
            Advancement.Builder builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
            this.criteria.forEach(builder::addCriterion);
            InstallBedrockModuleRecipe recipe = new InstallBedrockModuleRecipe(block);
            AdvancementHolder advancement = builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/"));
            recipeOutput.accept(id, recipe, advancement);
        }
    }
}
