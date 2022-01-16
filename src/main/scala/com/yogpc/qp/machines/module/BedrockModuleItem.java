package com.yogpc.qp.machines.module;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BedrockModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "remove_bedrock_module";

    public BedrockModuleItem() {
        super(new Properties().tab(Holder.TAB).fireResistant());
        setRegistryName(QuarryPlus.modID, NAME);
    }

    @Override
    public QuarryModule getModule(ItemStack stack) {
        return QuarryModule.Constant.BEDROCK;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltips, pIsAdvanced);
        tooltips.add(new TranslatableComponent("quarryplus.chat.bedrock_module_description"));
    }
}
