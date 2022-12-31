package com.yogpc.qp.data;

import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.recipe.QuarryBedrockModuleRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;

final class BedrockModuleRecipeBuilder implements RecipeBuilder {
    private final QPBlock target;
    private ResourceLocation saveName;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();

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
    public BedrockModuleRecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance) {
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

    @Override
    public void save(Consumer<FinishedRecipe> saveFunction, ResourceLocation recipeName) {
        this.advancement.parent(new ResourceLocation("recipes/root"))
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeName))
            .rewards(AdvancementRewards.Builder.recipe(recipeName))
            .requirements(RequirementsStrategy.OR);

        saveFunction.accept(new Result(this.saveName == null ? recipeName : this.saveName, this.target, this.advancement));
    }

    private record Result(ResourceLocation id, QPBlock target,
                          Advancement.Builder advancement) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject jsonObject) {
            jsonObject.addProperty("target", BuiltInRegistries.BLOCK.getKey(target).toString());
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return QuarryBedrockModuleRecipe.SERIALIZER;
        }

        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Override
        public ResourceLocation getAdvancementId() {
            var folder = "%1$s/%1$s".formatted(QuarryPlus.modID);
            return new ResourceLocation(getId().getNamespace(), "recipes/" + folder + "/" + getId().getPath());
        }
    }
}
