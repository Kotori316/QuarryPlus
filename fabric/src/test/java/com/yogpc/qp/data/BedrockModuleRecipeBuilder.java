package com.yogpc.qp.data;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.recipe.QuarryBedrockModuleRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Objects;

final class BedrockModuleRecipeBuilder implements RecipeBuilder {
    private final QPBlock target;
    private ResourceLocation saveName;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    BedrockModuleRecipeBuilder(QPBlock target, ResourceLocation saveName) {
        this.target = target;
        this.saveName = saveName;
    }

    BedrockModuleRecipeBuilder(QPBlock target) {
        this(target, null);
    }

    static BedrockModuleRecipeBuilder of(QPBlock target) {
        return new BedrockModuleRecipeBuilder(target);
    }

    BedrockModuleRecipeBuilder saveName(ResourceLocation saveName) {
        this.saveName = saveName;
        return this;
    }

    @Override
    public BedrockModuleRecipeBuilder unlockedBy(String string, Criterion<?> criterionTriggerInstance) {
        advancement.addCriterion(string, criterionTriggerInstance);
        return this;
    }

    @Override
    public BedrockModuleRecipeBuilder group(String group) {
        QuarryPlus.LOGGER.error("You can't set group({}) of BedrockModuleRecipe.", group);
        return this;
    }

    @Override
    public Item getResult() {
        return target.asItem();
    }

    @SuppressWarnings("removal")
    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation recipeName) {
        this.advancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeName))
            .rewards(AdvancementRewards.Builder.recipe(recipeName))
            .requirements(AdvancementRequirements.Strategy.OR);
        ResourceLocation name = Objects.requireNonNullElse(this.saveName, recipeName);

        recipeOutput.accept(name, new QuarryBedrockModuleRecipe(this.target), this.advancement.build(name));
    }
}
