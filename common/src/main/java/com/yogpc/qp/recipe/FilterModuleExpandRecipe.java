package com.yogpc.qp.recipe;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.module.FilterModuleItem;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FilterModuleExpandRecipe extends ShapelessRecipe {
    static final int MAX_ROWS = 6;
    static final int ROW_INCREMENT = 2;

    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "filter_module_expand_recipe");
    public static final RecipeSerializer<FilterModuleExpandRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(FilterModuleExpandRecipe::new);

    private final Item moduleItem;

    public FilterModuleExpandRecipe(Item moduleItem, CraftingBookCategory category) {
        super(LOCATION.toString(), category, new ItemStack(moduleItem), createIngredients(moduleItem));
        this.moduleItem = moduleItem;
    }

    FilterModuleExpandRecipe(CraftingBookCategory category) {
        this(PlatformAccess.getAccess().registerObjects().filterModuleItem().get(), category);
    }

    static NonNullList<Ingredient> createIngredients(Item moduleItem) {
        var chest = Ingredient.of(Items.CHEST);
        var module = Ingredient.of(moduleItem);
        return NonNullList.of(Ingredient.EMPTY,
            chest, chest, chest,
            chest, module, chest,
            chest, chest, chest
        );
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level)) {
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
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var module = findModule(input).copy();
        var rows = FilterModuleItem.getRowsFromStack(module);
        module.set(QuarryDataComponents.FILTER_MODULE_ROWS_COMPONENT, rows + ROW_INCREMENT);
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

    ItemStack findModule(CraftingInput input) {
        return input.items().stream()
            .filter(i -> i.getItem() == moduleItem)
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
        public Item getResult() {
            return PlatformAccess.getAccess().registerObjects().filterModuleItem().get();
        }

        @Override
        public void save(RecipeOutput recipeOutput, ResourceLocation id) {
            Advancement.Builder builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
            this.criteria.forEach(builder::addCriterion);
            FilterModuleExpandRecipe recipe = new FilterModuleExpandRecipe(CraftingBookCategory.MISC);
            AdvancementHolder advancement = builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/"));
            recipeOutput.accept(id, recipe, advancement);
        }
    }
}
