package com.yogpc.qp.machines;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QPItem extends Item {
    private final ResourceLocation registryName;

    public QPItem(@NotNull ResourceLocation registryName, Item.Properties properties) {
        super(properties);
        this.registryName = registryName;
    }

    @NotNull
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    /**
     * Implemented for unit test. Default implementation just returns "air".
     * This override return actual item name.
     */
    @Override
    public String toString() {
        return getRegistryName().getPath();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltips, pIsAdvanced);
        if (QuarryPlus.config != null && !QuarryPlus.config.enableMap.enabled(getRegistryName())) {
            tooltips.add(Component.translatable("quarryplus.tooltip.item_disable_message"));
        }
    }

    public List<ItemStack> creativeTabItem() {
        return List.of(new ItemStack(this));
    }
}
