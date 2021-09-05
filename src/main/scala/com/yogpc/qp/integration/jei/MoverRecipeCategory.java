package com.yogpc.qp.integration.jei;

import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.EnchantmentLevel;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

class MoverRecipeCategory implements IRecipeCategory<MoverRecipeCategory.MoverRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(QuarryPlus.modID, "quarryplus.enchantmover");
    private static final ResourceLocation backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/mover_jei.png");
    private static final int xOff = 0;
    private static final int yOff = 0;
    private static final int o = 18;
    private final IGuiHelper helper;
    private final List<ItemStack> pickaxes;

    public MoverRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.pickaxes = List.of(new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.NETHERITE_PICKAXE));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends MoverRecipe> getRecipeClass() {
        return MoverRecipe.class;
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
        return helper.createDrawableIngredient(new ItemStack(Holder.BLOCK_MOVER));
    }

    @Override
    public void setIngredients(MoverRecipe recipe, IIngredients ingredients) {
        var input = recipe.makeInput(pickaxes);
        var output = input.stream().map(Pair::getKey).map(recipe::makeOutput).toList();
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(input.stream().map(Pair::getValue).toList()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(output));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MoverRecipe recipe, IIngredients ingredients) {
        var stackGroup = recipeLayout.getItemStacks();

        stackGroup.init(0, true, 3, 30);
        stackGroup.init(1, false, 3 + 144, 30);

        stackGroup.set(ingredients);
    }

    @Override
    public void draw(MoverRecipe recipe, PoseStack stack, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, stack, mouseX, mouseY);
        var enchantments = recipe.item.acceptEnchantments().stream().map(e -> new EnchantmentLevel(e, 1))
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR).map(EnchantmentLevel::enchantment).toList();
        for (int i = 0; i < enchantments.size(); i++) {
            var text = new TranslatableComponent(enchantments.get(i).getDescriptionId());
            Minecraft.getInstance().font.draw(stack, text, 36 - xOff, 6 - yOff + 10 * i, 0x404040);
        }
    }

    static record MoverRecipe(EnchantableItem item, ItemStack stack) {
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
