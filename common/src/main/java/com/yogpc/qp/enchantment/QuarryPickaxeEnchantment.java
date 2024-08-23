package com.yogpc.qp.enchantment;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public final class QuarryPickaxeEnchantment {
    public static final String NAME = "quarry_pickaxe";
    public static final ResourceKey<Enchantment> KEY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME));
}
