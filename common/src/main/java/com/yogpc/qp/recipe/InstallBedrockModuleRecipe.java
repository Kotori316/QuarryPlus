package com.yogpc.qp.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.QpBlock;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Used only in fabric. Registered in all platforms.
 */
public final class InstallBedrockModuleRecipe extends NormalCraftingRecipe {
    public static final String NAME = "install_bedrock_module_recipe";
    public static final MapCodec<InstallBedrockModuleRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(o -> o.commonInfo, Recipe.CommonInfo.MAP_CODEC),
        RecordCodecBuilder.of(o -> o.bookInfo, CraftingRecipe.CraftingBookInfo.MAP_CODEC),
        RecordCodecBuilder.of(InstallBedrockModuleRecipe::getTargetBlockId, "target", Identifier.CODEC)
    ).apply(i, InstallBedrockModuleRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, InstallBedrockModuleRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
        Identifier.STREAM_CODEC, InstallBedrockModuleRecipe::getTargetBlockId,
        InstallBedrockModuleRecipe::new
    );
    public static final RecipeSerializer<InstallBedrockModuleRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final QpBlock block;
    final List<Ingredient> ingredients;

    public InstallBedrockModuleRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, QpBlock block) {
        super(commonInfo, bookInfo);
        this.block = block;
        this.ingredients = getIngredients(block);
    }

    InstallBedrockModuleRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, Identifier targetBlockId) {
        this(commonInfo, bookInfo, fromId(targetBlockId));
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!input.stackedContents().canCraft(this, null)) {
            return false;
        }
        // Check quarry's component
        var stack = input.items().stream().filter(s -> s.is(block.blockItem)).findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
        var installed = stack.getOrDefault(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, Boolean.FALSE);

        return !installed;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        var stack = input.items().stream().filter(s -> s.is(block.blockItem)).findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
        stack.set(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, true);
        return stack;
    }

    @Override
    public RecipeSerializer<InstallBedrockModuleRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(this.ingredients);
    }

    private static QpBlock fromId(Identifier blockId) {
        Block block = BuiltInRegistries.BLOCK.getValue(blockId);
        if (block instanceof QpBlock qpBlock) {
            return qpBlock;
        }
        throw new IllegalArgumentException("Invalid block %s(%s)".formatted(block, blockId));
    }

    Identifier getTargetBlockId() {
        return block.name;
    }

    private static @NotNull NonNullList<Ingredient> getIngredients(QpBlock block) {
        return NonNullList.of(Ingredient.of(block), Ingredient.of(PlatformAccess.getAccess().registerObjects().bedrockModuleItem().get()));
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
        public ResourceKey<Recipe<?>> defaultId() {
            return RecipeBuilder.getDefaultRecipeId(new ItemStack(block.blockItem));
        }

        @Override
        public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> resourceKey) {
            Advancement.Builder builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceKey))
                .rewards(AdvancementRewards.Builder.recipe(resourceKey))
                .requirements(AdvancementRequirements.Strategy.OR);
            this.criteria.forEach(builder::addCriterion);
            InstallBedrockModuleRecipe recipe = new InstallBedrockModuleRecipe(
                new Recipe.CommonInfo(false),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, QuarryPlus.modID + ":" + NAME),
                block
            );
            AdvancementHolder advancement = builder.build(resourceKey.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/"));
            recipeOutput.accept(resourceKey, recipe, advancement);
        }
    }
}
