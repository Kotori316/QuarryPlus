package com.yogpc.qp.machines.workbench;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class WorkbenchRecipe implements Recipe<TileWorkbench> {
    public static final ResourceLocation recipeLocation = new ResourceLocation(QuarryPlus.modID, "workbench_recipe");
    public static final WorkbenchRecipeSerializer SERIALIZER = new WorkbenchRecipeSerializer();
    public static final RecipeType<WorkbenchRecipe> RECIPE_TYPE = new WorkbenchRecipeType();
    @VisibleForTesting
    static RecipeFinder recipeFinder = new DefaultFinder();
    public static final Comparator<RecipeHolder<WorkbenchRecipe>> COMPARATOR =
        Comparator.<RecipeHolder<WorkbenchRecipe>, WorkbenchRecipe>comparing(RecipeHolder::value,
                Comparator.comparingLong(WorkbenchRecipe::getRequiredEnergy)
                    .thenComparingInt(r -> Item.getId(r.output.getItem())))
            .thenComparing(RecipeHolder::id);

    public final ItemStack output;
    private final long energy;
    private final boolean showInJEI;

    public WorkbenchRecipe(ItemStack output, long energy, boolean showInJEI) {
        this.output = output;
        this.energy = energy;
        this.showInJEI = showInJEI;
    }

    @Override
    public String toString() {
        return "WorkbenchRecipe{output=" + output + ", energy=" + energy + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkbenchRecipe that = (WorkbenchRecipe) o;
        return energy == that.energy && ItemStack.matches(this.output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.output, energy);
    }

    public final long getRequiredEnergy() {
        return energy;
    }

    public final boolean showInJEI() {
        return showInJEI;
    }

    // Recipe interface implementation.
    @Override
    public final boolean matches(TileWorkbench workbench, Level level) {
        return hasContent() && hasAllRequiredItems(workbench.ingredientInventory);
    }

    @Override
    public final ItemStack assemble(TileWorkbench workbench, RegistryAccess access) {
        return getOutput(workbench.ingredientInventory, access);
    }

    @Override
    public final boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public final ItemStack getResultItem(RegistryAccess access) {
        return output;
    }

    @Override
    public final RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public final RecipeType<?> getType() {
        return RECIPE_TYPE;
    }

    @Override
    public final boolean isSpecial() {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputs().stream().map(IngredientList::asIngredient).collect(Collectors.toCollection(NonNullList::create));
    }

    public static WorkbenchRecipe dummyRecipe() {
        return DummyRecipe.INSTANCE;
    }

    public static RecipeFinder getRecipeFinder() {
        return recipeFinder;
    }

    // Workbench Recipe methods.
    public abstract List<IngredientList> inputs();

    public boolean hasContent() {
        return true;
    }

    protected abstract String getSubTypeName();

    protected abstract ItemStack getOutput(List<ItemStack> inventory, RegistryAccess access);

    protected boolean hasAllRequiredItems(List<ItemStack> inventory) {
        var copied = inventory.stream().map(ItemStack::copy).toList();
        for (IngredientList input : this.inputs()) {
            var found = copied.stream().anyMatch(input::shrink);
            if (!found) return false;
        }
        return copied.stream().allMatch(i -> i.getCount() >= 0);
    }

    public void consumeItems(List<ItemStack> inventory) {
        for (IngredientList input : inputs()) {
            for (ItemStack stack : inventory) {
                if (input.shrink(stack)) break;
            }
        }
    }

    private static final class WorkbenchRecipeType implements RecipeType<WorkbenchRecipe> {
        @Override
        public String toString() {
            return recipeLocation.toString();
        }
    }
}
