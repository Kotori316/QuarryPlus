package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.world.item.ItemStack;

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
}
