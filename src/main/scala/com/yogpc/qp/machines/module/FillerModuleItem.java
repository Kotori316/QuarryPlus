package com.yogpc.qp.machines.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class FillerModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "filler_module";

    public FillerModuleItem() {
        super(new ResourceLocation(QuarryPlus.modID, NAME), new Properties());
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        return QuarryModule.Constant.FILLER;
    }
}
