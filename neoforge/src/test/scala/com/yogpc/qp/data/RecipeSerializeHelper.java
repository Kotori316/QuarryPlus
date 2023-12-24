package com.yogpc.qp.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

record RecipeSerializeHelper(
    Recipe<?> recipe,
    List<ICondition> conditions,
    @NotNull ResourceLocation saveName
) implements DataBuilder {
    @Override
    public ResourceLocation location() {
        return saveName;
    }

    @Override
    public JsonElement build(HolderLookup.Provider provider) {
        var o = Util.getOrThrow(Recipe.CODEC.encodeStart(JsonOps.INSTANCE, recipe).map(JsonElement::getAsJsonObject), IllegalStateException::new);
        if (!conditions.isEmpty()) {
            ICondition.writeConditions(provider, o, conditions);
        }
        return o;
    }

    RecipeSerializeHelper addCondition(ICondition condition) {
        var copy = new ArrayList<>(conditions);
        copy.add(condition);
        return new RecipeSerializeHelper(recipe, copy, saveName);
    }

    static RecipeSerializeHelper by(RecipeBuilder c, @NotNull ResourceLocation saveName) {
        return new RecipeSerializeHelper(getConsumeValue(c), Collections.emptyList(), saveName);
    }

    static RecipeSerializeHelper by(Recipe<?> recipe, @NotNull ResourceLocation location) {
        return new RecipeSerializeHelper(recipe, Collections.emptyList(), location);
    }

    static Recipe<?> getConsumeValue(RecipeBuilder c) {
        c.unlockedBy("dummy", RecipeUnlockedTrigger.unlocked(QuarryPlusDataProvider.location("dummy")));
        AtomicReference<Recipe<?>> reference = new AtomicReference<>();
        c.save(new RecipeOutput() {
            @Override
            public void accept(ResourceLocation resourceLocation, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder, ICondition... iConditions) {
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
