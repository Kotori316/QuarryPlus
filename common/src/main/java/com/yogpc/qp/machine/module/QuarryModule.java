package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public interface QuarryModule {
    ResourceLocation moduleId();

    enum Constant implements QuarryModule {
        DUMMY,
        PUMP,
        BEDROCK,
        ;

        Constant() {
        }

        @Override
        public ResourceLocation moduleId() {
            return ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, name().toLowerCase(Locale.ROOT) + "_module");
        }
    }
}
