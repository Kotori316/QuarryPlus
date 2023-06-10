package com.yogpc.qp.machines.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BedrockModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "remove_bedrock_module";

    public BedrockModuleItem() {
        super(new ResourceLocation(QuarryPlus.modID, NAME), new Properties().fireResistant());
    }

    @Override
    public QuarryModule getModule(ItemStack stack) {
        return QuarryModule.Constant.BEDROCK;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltips, pIsAdvanced);
        tooltips.add(Component.translatable("quarryplus.chat.bedrock_module_description"));
    }
}
