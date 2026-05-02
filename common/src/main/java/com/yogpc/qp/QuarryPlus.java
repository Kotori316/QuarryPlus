package com.yogpc.qp;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuarryPlus {
    public static final String modID = "quarryplus";
    public static final String MOD_NAME = "QuarryPlus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static CreativeModeTab.Builder buildCreativeModeTab(CreativeModeTab.Builder builder, CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
        return builder.icon(() -> new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get()))
            .title(Component.translatable("itemGroup.%s".formatted(modID)))
            .displayItems(displayItemsGenerator);
    }

    public static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modID, name));
    }

    public static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modID, name));
    }
}
