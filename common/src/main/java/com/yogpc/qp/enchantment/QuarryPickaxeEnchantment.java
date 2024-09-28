package com.yogpc.qp.enchantment;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public final class QuarryPickaxeEnchantment {
    public static final String NAME = "quarry_pickaxe";
    public static final ResourceKey<Enchantment> KEY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME));

    public static Enchantment createEnchantment(BootstrapContext<Enchantment> context, TagKey<Item> quarryPickaxeTag) {
        var definition = Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(quarryPickaxeTag),
            1,
            1,
            Enchantment.constantCost(25), Enchantment.constantCost(50),
            1,
            EquipmentSlotGroup.HAND
        );
        return Enchantment.enchantment(definition).build(KEY.location());
    }
}
