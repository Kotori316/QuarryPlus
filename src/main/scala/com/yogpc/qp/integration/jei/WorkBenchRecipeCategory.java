package com.yogpc.qp.integration.jei;

import java.util.Collections;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.workbench.IngredientList;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

class WorkBenchRecipeCategory implements IRecipeCategory<WorkbenchRecipe> {
    public static final RecipeType<WorkbenchRecipe> RECIPE_TYPE = RecipeType.create(QuarryPlus.modID, "jei_workbenchplus", WorkbenchRecipe.class);
    private static final ResourceLocation backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench_jei2.png");
    private static final int xOff = 0;
    private static final int yOff = 0;
    private final IGuiHelper helper;
    private final IDrawableAnimated animateBar;

    WorkBenchRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        IDrawableStatic bar = helper.createDrawable(backGround, xOff, 87, 160, 4);
        this.animateBar = helper.createAnimatedDrawable(bar, 300, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<WorkbenchRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Holder.BLOCK_WORKBENCH.getName();
    }

    @Override
    public IDrawable getBackground() {
        return helper.createDrawable(backGround, xOff, yOff, 167, 86);
    }

    @Override
    public IDrawable getIcon() {
        return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Holder.BLOCK_WORKBENCH));
    }

    @Override
    public void draw(WorkbenchRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        animateBar.draw(stack, 4, 60);
        Minecraft.getInstance().font.draw(stack, (recipe.getRequiredEnergy() / PowerTile.ONE_FE) + "MJ", 36 - xOff, 70 - yOff, 0x404040);
        // Enchantment copy
        // Minecraft.getInstance().font.drawString(matrixStack, (recipe.energy.toDouble / APowerTile.MJToMicroMJ).toString + "MJ", 36 - xOff, 67 - yOff, 0x404040)
        // Minecraft.getInstance().font.drawString(matrixStack, "Keeps enchantments", 36 - xOff, 77 - yOff, 0x404040)
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorkbenchRecipe recipe, IFocusGroup focuses) {
        var input = recipe.inputs().stream()
            .map(IngredientList::stackList)
            .toList();
        var output = Collections.singletonList(recipe.output);

        int x0 = 4;
        final int o = 18;
        for (int i = 0; i < recipe.inputs().size(); i++) {
            int xIndex = i % 9;
            int yIndex = i / 9;
            var slotInput = input.get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, x0 + o * xIndex - xOff, x0 + o * yIndex - yOff)
                .addItemStacks(slotInput);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, x0 - xOff, x0 + 64 - yOff)
            .addItemStacks(output);
    }
}
