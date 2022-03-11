package com.yogpc.qp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonElement;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

record RecipeSerializeHelper(
    FinishedRecipe recipe,
    List<ICondition> conditions,
    @Nullable ResourceLocation saveName
) implements DataBuilder {
    @Override
    public ResourceLocation location() {
        return saveName == null ? recipe.getId() : saveName;
    }

    @Override
    public JsonElement build() {
        var o = recipe.serializeRecipe();
        if (!conditions.isEmpty()) {
            o.add("conditions", conditions.stream().map(CraftingHelper::serialize).collect(MapMulti.jsonArrayCollector()));
        }
        return o;
    }

    RecipeSerializeHelper addCondition(ICondition condition) {
        var copy = new ArrayList<>(conditions);
        copy.add(condition);
        return new RecipeSerializeHelper(recipe, copy, saveName);
    }

    static RecipeSerializeHelper by(ShapedRecipeBuilder c, ResourceLocation saveName) {
        return new RecipeSerializeHelper(getConsumeValue(c), Collections.emptyList(), saveName);
    }

    static RecipeSerializeHelper by(ShapelessRecipeBuilder c, ResourceLocation saveName) {
        return new RecipeSerializeHelper(getConsumeValue(c), Collections.emptyList(), saveName);
    }

    static RecipeSerializeHelper by(FinishedRecipe recipe) {
        return new RecipeSerializeHelper(recipe, Collections.emptyList(), null);
    }

    static FinishedRecipe getConsumeValue(ShapedRecipeBuilder c) {
        c.unlockedBy("dummy", RecipeUnlockedTrigger.unlocked(QuarryPlusDataProvider.location("dummy")));
        AtomicReference<FinishedRecipe> reference = new AtomicReference<>();
        c.save(reference::set);
        return reference.get();
    }

    static FinishedRecipe getConsumeValue(ShapelessRecipeBuilder c) {
        c.unlockedBy("dummy", RecipeUnlockedTrigger.unlocked(QuarryPlusDataProvider.location("dummy")));
        AtomicReference<FinishedRecipe> reference = new AtomicReference<>();
        c.save(reference::set);
        return reference.get();
    }
}
