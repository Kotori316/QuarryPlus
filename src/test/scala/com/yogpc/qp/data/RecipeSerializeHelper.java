package com.yogpc.qp.data;

import com.google.gson.JsonElement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

record RecipeSerializeHelper(
    FinishedRecipe recipe,
    List<ICondition> conditions,
    @Nullable ResourceLocation saveName
) implements DataBuilder {
    @Override
    public ResourceLocation location() {
        return saveName == null ? recipe.id() : saveName;
    }

    @Override
    public JsonElement build(HolderLookup.Provider provider) {
        var o = recipe.serializeRecipe();
        if (!conditions.isEmpty()) {
            ICondition.writeConditions(provider, o, "conditions", conditions);
        }
        return o;
    }

    RecipeSerializeHelper addCondition(ICondition condition) {
        var copy = new ArrayList<>(conditions);
        copy.add(condition);
        return new RecipeSerializeHelper(recipe, copy, saveName);
    }

    static RecipeSerializeHelper by(RecipeBuilder c, ResourceLocation saveName) {
        return new RecipeSerializeHelper(getConsumeValue(c), Collections.emptyList(), saveName);
    }

    static RecipeSerializeHelper by(FinishedRecipe recipe) {
        return new RecipeSerializeHelper(recipe, Collections.emptyList(), null);
    }

    static FinishedRecipe getConsumeValue(RecipeBuilder c) {
        c.unlockedBy("dummy", RecipeUnlockedTrigger.unlocked(QuarryPlusDataProvider.location("dummy")));
        AtomicReference<FinishedRecipe> reference = new AtomicReference<>();
        c.save(new RecipeOutput() {
            @Override
            public void accept(FinishedRecipe recipe, ICondition... conditions) {
                reference.set(recipe);
            }

            @Override
            public Advancement.Builder advancement() {
                return Advancement.Builder.recipeAdvancement();
            }
        });
        return reference.get();
    }
}
