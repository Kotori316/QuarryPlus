package com.yogpc.qp.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.machines.EnchantableItem;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {

    @Inject(method = "getAvailableEnchantmentResults", at = @At("HEAD"), cancellable = true)
    private static void getPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (stack.getItem() instanceof EnchantableItem enchantableItem) {
            List<EnchantmentInstance> entries = Registry.ENCHANTMENT.stream()
                .filter(e -> !e.isTreasureOnly() || treasureAllowed)
                .filter(Enchantment::isDiscoverable)
                .filter(enchantableItem)
                .flatMap(e ->
                    IntStream.iterate(e.getMaxLevel(), i -> i >= e.getMinLevel(), i -> i - 1)
                        .filter(level -> e.getMinCost(level) <= power && power <= e.getMaxCost(level))
                        .mapToObj(level -> new EnchantmentInstance(e, level))
                        .findFirst()
                        .stream())
                .collect(Collectors.toList());
            cir.setReturnValue(new ArrayList<>(entries)); // This method must return mutable list.
        }
    }
}
