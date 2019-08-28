/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.machines.base;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import javax.annotation.Nonnull;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraftforge.registries.ForgeRegistries;

import static jp.t2v.lab.syntax.MapStreamSyntax.byEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.byKey;
import static jp.t2v.lab.syntax.MapStreamSyntax.entry;
import static jp.t2v.lab.syntax.MapStreamSyntax.keys;
import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;
import static jp.t2v.lab.syntax.MapStreamSyntax.toAny;
import static jp.t2v.lab.syntax.MapStreamSyntax.toEntry;

public interface IEnchantableTile {

    ResourceLocation FortuneID = ForgeRegistries.ENCHANTMENTS.getKey(Enchantments.FORTUNE);
    ResourceLocation SilktouchID = ForgeRegistries.ENCHANTMENTS.getKey(Enchantments.SILK_TOUCH);
    ResourceLocation EfficiencyID = ForgeRegistries.ENCHANTMENTS.getKey(Enchantments.EFFICIENCY);
    ResourceLocation UnbreakingID = ForgeRegistries.ENCHANTMENTS.getKey(Enchantments.UNBREAKING);
    BiPredicate<ResourceLocation, Integer> isValidEnch = (id, level) -> {
        if (id.equals(FortuneID) || id.equals(UnbreakingID))
            return level <= 3;
        else if (id.equals(EfficiencyID))
            return level <= 5;
        else
            return id.equals(SilktouchID) && level == 1;
    };

    /**
     * Called after enchantment setting.
     */
    void G_ReInit();

    /**
     * @return Map (Enchantment id, level)
     */
    @Nonnull
    Map<ResourceLocation, Integer> getEnchantments();

    /**
     * @param id  Enchantment id
     * @param val level
     */
    void setEnchantment(ResourceLocation id, short val);

    default void sendEnchantMassage(PlayerEntity player) {
        Util.getEnchantmentsChat(this).forEach(c -> player.sendStatusMessage(c, false));
    }

    default ItemStack getEnchantedPickaxe() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        getEnchantments().entrySet().stream()
            .filter(byEntry(/*Config.common().disabled().apply(BlockBookMover.SYMBOL).get()*/ false ? isValidEnch : (k, v) -> true))
            .map(keys(ForgeRegistries.ENCHANTMENTS::getValue))
            .filter(byKey(Objects::nonNull))
            .forEach(entry(stack::addEnchantment));
        return stack;
    }

    //Move static methods to this inner class because static method in an interface is not supported by Scala 2.11.1.
    class Util {

        public static void init(@Nonnull final IEnchantableTile te, @Nonnull final ListNBT tagList) {
            tagList.stream()
                .map(CompoundNBT.class::cast)
                .map(toEntry(n -> n.getString("id"), n -> n.getShort("lvl")))
                .map(keys(ResourceLocation::new))
                .forEach(entry(te::setEnchantment));
            te.G_ReInit();
        }

        static List<ITextComponent> getEnchantmentsChat(@Nonnull final IEnchantableTile te) {
            final Map<ResourceLocation, Integer> enchantments = te.getEnchantments();
            if (enchantments.size() <= 0) {
                return Collections.singletonList(new TranslationTextComponent(TranslationKeys.PLUSENCHANTNO));
            } else {
                LinkedList<ITextComponent> collect = enchantments.entrySet().stream()
                    .map(keys(ForgeRegistries.ENCHANTMENTS::getValue))
                    .filter(byKey(Objects::nonNull)).map(toAny((enchantment, level) ->
                        new TranslationTextComponent(TranslationKeys.INDENT, new TranslationTextComponent(enchantment.getName()),
                            enchantment.getMaxLevel() != 1
                                ? new TranslationTextComponent(TranslationKeys.ENCHANT_LEVELS.getOrDefault(level, level.toString()))
                                : ""))).collect(Collectors.toCollection(LinkedList::new));
                collect.addFirst(new TranslationTextComponent(TranslationKeys.PLUSENCHANT));
                return collect;
            }
        }

        public static void enchantmentToIS(@Nonnull final IEnchantableTile te, @Nonnull final ItemStack is) {
            te.getEnchantments().entrySet().stream()
                .map(keys(ForgeRegistries.ENCHANTMENTS::getValue))
                .filter(byKey(Objects::nonNull))
                .forEach(entry(is::addEnchantment));
        }

        @Nonnull
        public static <T extends IEnchantableTile> Consumer<T> initConsumer(ItemStack stack) {
            return t -> init(t, stack.getEnchantmentTagList());
        }
    }

    class DropFunction extends LootFunction {
        public static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "drop_function");

        protected DropFunction(ILootCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        protected ItemStack doApply(ItemStack stack, LootContext context) {
            Optional.ofNullable(context.get(LootParameters.BLOCK_ENTITY))
                .flatMap(optCast(IEnchantableTile.class))
                .ifPresent(t -> IEnchantableTile.Util.enchantmentToIS(t, stack));
            return stack;
        }

        public static class Serializer extends LootFunction.Serializer<DropFunction> {
            public Serializer() {
                super(DropFunction.LOCATION, DropFunction.class);
            }

            @Override
            public DropFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn) {
                return new DropFunction(conditionsIn);
            }
        }
    }
}
