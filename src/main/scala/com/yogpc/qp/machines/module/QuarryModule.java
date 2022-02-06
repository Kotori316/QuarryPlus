package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;

public interface QuarryModule {
    ResourceLocation moduleId();

    enum Constant implements QuarryModule {
        DUMMY(new ResourceLocation(QuarryPlus.modID, "dummy")),
        PUMP(Holder.BLOCK_PUMP.getRegistryName()),
        BEDROCK(Holder.ITEM_BEDROCK_MODULE.getRegistryName()),
        FILLER(Holder.ITEM_FILLER_MODULE.getRegistryName()),
        ;

        private final ResourceLocation moduleId;

        Constant(ResourceLocation moduleId) {
            this.moduleId = moduleId;
        }

        @Override
        public ResourceLocation moduleId() {
            return moduleId;
        }
    }
}
