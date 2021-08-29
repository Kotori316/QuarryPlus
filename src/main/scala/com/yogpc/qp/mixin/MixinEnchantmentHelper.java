package com.yogpc.qp.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.machines.EnchantableItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {

    @Inject(method = "getPossibleEntries", at = @At("HEAD"), cancellable = true)
    private static void getPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (stack.getItem() instanceof EnchantableItem enchantableItem) {
            List<EnchantmentLevelEntry> entries = Registry.ENCHANTMENT.stream()
                .filter(e -> !e.isTreasure() || treasureAllowed)
                .filter(Enchantment::isAvailableForRandomSelection)
                .filter(enchantableItem)
                .flatMap(e ->
                    IntStream.iterate(e.getMaxLevel(), i -> i >= e.getMinLevel(), i -> i - 1)
                        .filter(level -> e.getMinPower(level) <= power && power <= e.getMaxPower(level))
                        .mapToObj(level -> new EnchantmentLevelEntry(e, level))
                        .findFirst()
                        .stream())
                .collect(Collectors.toList());
            cir.setReturnValue(new ArrayList<>(entries)); // This method must return mutable list.
        }
    }
}
