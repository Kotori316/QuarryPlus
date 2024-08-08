package com.yogpc.qp;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public final class QuarryPlus {
    public static final String modID = "quarryplus";
    public static final String MOD_NAME = "QuarryPlus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static CreativeModeTab.Builder buildCreativeModeTab(CreativeModeTab.Builder builder) {
        return builder.icon(() -> new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get()))
            .title(Component.translatable("itemGroup.%s".formatted(modID)))
            .displayItems((itemDisplayParameters, output) -> {
                PlatformAccess.getAccess().registerObjects().allItems()
                    .map(Supplier::get)
                    .flatMap(inCreativeTabs -> inCreativeTabs.creativeTabItem(itemDisplayParameters))
                    .forEach(output::accept);
            });
    }
}
