package com.yogpc.qp.integration.crafttweaker;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.recipe.WorkbenchRecipe;
import com.yogpc.qp.tile.ItemDamage;
import com.yogpc.qp.utils.IngredientWithCount;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass(WorkBenchCTRegister.packageName)
public class WorkBenchCTRegister {
    public static final String packageName = "mods." + QuarryPlus.modID + ".WorkbenchPlus";
    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private WorkBenchCTRegister() {
    }

    @ZenMethod
    public static void addRecipe(IIngredient input, IItemStack output, float energy) {
        addRecipe(new IIngredient[]{input}, output, energy);
    }

    @ZenMethod
    public static void addRecipe(IIngredient[] inputs, IItemStack output, float energy) {
        final List<List<IngredientWithCount>> inputsList = Stream.of(inputs)
            .filter(Objects::nonNull)
            .map(i -> new IngredientWithCount(new CTIngredient(i), i.getAmount()))
            .map(Collections::singletonList)
            .collect(Collectors.toList());

        Optional.ofNullable(output).flatMap(i ->
            i.getItems().stream()
                .flatMap(WorkBenchCTRegister::stackList)
                .findFirst()
        ).ifPresent(out -> CT.actions.add(new Add(out, inputsList, (double) energy)));
    }

    @ZenMethod
    public static void removeRecipe(IItemStack output) {
        ItemDamage itemDamage = ItemDamage.apply(CraftTweakerMC.getItemStack(output));
        CT.actions.add(new RemoveByOut(itemDamage));
    }

    @ZenMethod
    public static void removeRecipe(String recipeId) {
        ResourceLocation name = new ResourceLocation(recipeId);
        CT.actions.add(new RemoveById(name));
    }

    private static Stream<ItemStack> stackList(@Nullable IItemStack stack) {
        if (stack == null) {
            return Stream.empty();
        }
        return stack.getItems().stream()
            .map(CraftTweakerMC::getItemStack)
            .filter(Objects::nonNull)
            .filter(MapStreamSyntax.not(ItemStack::isEmpty));
    }

    private static class Add implements IAction {
        private final ItemStack out;
        private final List<List<IngredientWithCount>> inputs;
        private final double energy;

        private Add(@Nonnull ItemStack out, @Nonnull List<List<IngredientWithCount>> inputs, double energy) {
            this.out = out;
            this.inputs = inputs;
            this.energy = energy;
        }

        @Override
        public void apply() {
            ResourceLocation name = new ResourceLocation(CT.CRAFT_TWEAKER_ID, "quarryplus_workbench_" + COUNTER.getAndIncrement());
            WorkbenchRecipe.addIngredientRecipe(name, out, energy, inputs, true);
        }

        @Override
        public String describe() {
            return "QuarryPlus CTAdd i:" + inputs + " o:" + out;
        }
    }

    private static class RemoveByOut implements IAction {
        private final ItemDamage output;

        public RemoveByOut(@Nonnull ItemDamage output) {
            this.output = output;
        }

        @Override
        public void apply() {
            WorkbenchRecipe.removeRecipe(output);
        }

        @Override
        public String describe() {
            return "QuarryPlus CTRemoveByOut o:" + output;
        }
    }

    private static class RemoveById implements IAction {
        private final ResourceLocation name;

        public RemoveById(@Nonnull ResourceLocation name) {
            this.name = name;
        }

        @Override
        public void apply() {
            WorkbenchRecipe.removeRecipe(name);
        }

        @Override
        public String describe() {
            return "QuarryPlus CTRemoveById id:" + name;
        }
    }

    private static class CTIngredient extends Ingredient {
        @Nonnull
        private final IIngredient ingredient;

        public CTIngredient(@Nonnull IIngredient ingredient) {
            super(CraftTweakerMC.getItemStacks(ingredient.getItemArray()));
            this.ingredient = ingredient;
        }

        @Override
        public boolean apply(@Nullable ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return false;
            }
            return ingredient.matches(CraftTweakerMC.getIItemStack(stack));
        }

        @Override
        public boolean isSimple() {
            return false;
        }
    }
}
