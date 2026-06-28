package com.yogpc.qp.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.module.FilterModuleItem;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FilterModuleExpandRecipe extends NormalCraftingRecipe {
    static final int MAX_ROWS = 6;
    static final int ROW_INCREMENT = 2;

    public static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "filter_module_expand_recipe");
    public static final MapCodec<FilterModuleExpandRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter((o) -> o.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter((o) -> o.bookInfo),
            ItemStackTemplate.MAP_CODEC.fieldOf("moduleItem").forGetter(o -> o.moduleItem)
        ).apply(i, FilterModuleExpandRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterModuleExpandRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
        ItemStackTemplate.STREAM_CODEC, o -> o.moduleItem,
        FilterModuleExpandRecipe::new
    );
    public static final RecipeSerializer<FilterModuleExpandRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate moduleItem;
    private final ShapedRecipePattern pattern;

    public FilterModuleExpandRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate moduleItem) {
        super(commonInfo, bookInfo);
        this.moduleItem = moduleItem;
        this.pattern = createPattern(moduleItem.item().value());
    }

    static ShapedRecipePattern createPattern(Item moduleItem) {
        return ShapedRecipePattern.of(
            Map.of(
                'c', Ingredient.of(Items.CHEST),
                'm', Ingredient.of(moduleItem)
            ),
            "ccc",
            "cmc",
            "ccc"
        );
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!this.pattern.matches(input)) {
            return false;
        }
        var module = findModule(input);
        if (module.isEmpty()) {
            return false;
        }
        var rows = FilterModuleItem.getRowsFromStack(module);
        return rows + ROW_INCREMENT <= MAX_ROWS;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        var module = findModule(input).copy();
        var rows = FilterModuleItem.getRowsFromStack(module);
        module.set(QuarryDataComponents.FILTER_MODULE_ROWS_COMPONENT, rows + ROW_INCREMENT);
        return module;
    }

    @Override
    public RecipeSerializer<FilterModuleExpandRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(this.pattern.ingredients());
    }

    ItemStack findModule(CraftingInput input) {
        return input.items().stream()
            .filter(i -> moduleItem.is(i.getItem()))
            .findAny()
            .orElse(ItemStack.EMPTY);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements RecipeBuilder {
        private final RecipeCategory category = RecipeCategory.MISC;
        private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

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
            return ResourceKey.create(Registries.RECIPE, LOCATION);
        }

        @Override
        public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
            Advancement.Builder builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
            this.criteria.forEach(builder::addCriterion);
            FilterModuleExpandRecipe recipe = new FilterModuleExpandRecipe(
                new Recipe.CommonInfo(false),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                new ItemStackTemplate(PlatformAccess.getAccess().registerObjects().filterModuleItem().get())
            );
            AdvancementHolder advancement = builder.build(id.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/"));
            recipeOutput.accept(id, recipe, advancement);
        }
    }
}
