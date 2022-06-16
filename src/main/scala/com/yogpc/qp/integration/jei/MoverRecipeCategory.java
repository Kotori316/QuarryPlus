package com.yogpc.qp.integration.jei;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.EnchantmentLevel;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

class MoverRecipeCategory implements IRecipeCategory<MoverRecipeCategory.MoverRecipe> {
    public static final RecipeType<MoverRecipe> RECIPE_TYPE = RecipeType.create(QuarryPlus.modID, "quarryplus.enchantmover", MoverRecipe.class);
    private static final ResourceLocation backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/mover_jei.png");
    private static final int xOff = 0;
    private static final int yOff = 0;
    private final IGuiHelper helper;
    private final List<ItemStack> pickaxes;

    public MoverRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.pickaxes = List.of(new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.NETHERITE_PICKAXE));
    }

    @Override
    public RecipeType<MoverRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Holder.BLOCK_MOVER.getName();
    }

    @Override
    public IDrawable getBackground() {
        return helper.createDrawable(backGround, xOff, yOff, 167, 76);
    }

    @Override
    public IDrawable getIcon() {
        return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Holder.BLOCK_MOVER));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MoverRecipe recipe, IFocusGroup focuses) {
        var input = recipe.makeInput(pickaxes);
        var output = input.stream().map(Pair::getKey).map(recipe::makeOutput).toList();

        builder.addSlot(RecipeIngredientRole.INPUT, 4, 31)
            .addItemStacks(input.stream().map(Pair::getValue).toList());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 4 + 144, 31)
            .addItemStacks(output);
    }

    @Override
    public void draw(MoverRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        var enchantments = recipe.item.acceptEnchantments().stream().map(e -> new EnchantmentLevel(e, 1))
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR).map(EnchantmentLevel::enchantment).toList();
        for (int i = 0; i < enchantments.size(); i++) {
            var text = Component.translatable(enchantments.get(i).getDescriptionId());
            Minecraft.getInstance().font.draw(stack, text, 36 - xOff, 6 - yOff + 10 * i, 0x404040);
        }
    }

    record MoverRecipe(EnchantableItem item, ItemStack stack) {
        List<Pair<Enchantment, ItemStack>> makeInput(List<ItemStack> pickaxes) {
            return item.acceptEnchantments().stream()
                .flatMap(e -> pickaxes.stream().map(ItemStack::copy).peek(i -> i.enchant(e, e.getMaxLevel()))
                    .map(i -> Pair.of(e, i)))
                .toList();
        }

        ItemStack makeOutput(Enchantment enchantment) {
            var s = stack.copy();
            s.enchant(enchantment, enchantment.getMaxLevel());
            return s;
        }
    }

    static List<MoverRecipe> recipes() {
        return ForgeRegistries.ITEMS.getValues().stream()
            .filter(i -> i instanceof EnchantableItem)
            .map(i -> new MoverRecipe((EnchantableItem) i, new ItemStack(i)))
            .toList();
    }
}
