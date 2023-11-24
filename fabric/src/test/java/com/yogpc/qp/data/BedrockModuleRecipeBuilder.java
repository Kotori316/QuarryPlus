package com.yogpc.qp.data;

import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.recipe.QuarryBedrockModuleRecipe;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;

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

        recipeOutput.accept(new Result(this.saveName == null ? recipeName : this.saveName, this.target, this.advancement));
    }

    private record Result(ResourceLocation id, QPBlock target,
                          Advancement.Builder builder) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject jsonObject) {
            jsonObject.addProperty("target", BuiltInRegistries.BLOCK.getKey(target).toString());
        }

        @Override
        public ResourceLocation id() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> type() {
            return QuarryBedrockModuleRecipe.SERIALIZER;
        }

        @Override
        public AdvancementHolder advancement() {
            return this.builder.build(getAdvancementId());
        }

        public ResourceLocation getAdvancementId() {
            var folder = "%1$s/%1$s".formatted(QuarryPlus.modID);
            return new ResourceLocation(id().getNamespace(), "recipes/" + folder + "/" + id().getPath());
        }
    }
}
