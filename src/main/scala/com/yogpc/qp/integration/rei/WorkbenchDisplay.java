package com.yogpc.qp.integration.rei;

import java.util.List;
import java.util.Optional;

import com.yogpc.qp.machines.workbench.IngredientList;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // Required by API
final class WorkbenchDisplay extends BasicDisplay {
    final long energy;

    WorkbenchDisplay(WorkbenchRecipe recipe) {
        this(
            recipe.inputs().stream()
                .map(IngredientList::stackList)
                .map(EntryIngredients::ofItemStacks)
                .toList(),
            List.of(EntryIngredients.of(recipe.getResultItem())),
            Optional.of(recipe.getId()),
            recipe.getRequiredEnergy()
        );
    }

    WorkbenchDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, long energy) {
        super(inputs, outputs, location);
        this.energy = energy;
    }

    WorkbenchDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, CompoundTag tag) {
        this(inputs, outputs, location, tag.getLong("energy"));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return QuarryReiPlugin.WORKBENCH;
    }

    public static BasicDisplay.Serializer<WorkbenchDisplay> serializer() {
        return BasicDisplay.Serializer.of(WorkbenchDisplay::new, WorkbenchDisplay::saveTagData);
    }

    private static void saveTagData(WorkbenchDisplay display, CompoundTag tag) {
        tag.putLong("energy", display.energy);
    }
}