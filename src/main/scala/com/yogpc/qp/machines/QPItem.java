package com.yogpc.qp.machines;

import java.util.List;
import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class QPItem extends Item {
    public QPItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * Implemented for unit test. Default implementation just returns "air".
     * This override return actual item name.
     */
    @Override
    public String toString() {
        return Objects.requireNonNull(getRegistryName()).getPath();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltips, pIsAdvanced);
        if (!QuarryPlus.config.enableMap.enabled(getRegistryName())) {
            tooltips.add(new TranslatableComponent("quarryplus.tooltip.item_disable_message"));
        }
    }
}
