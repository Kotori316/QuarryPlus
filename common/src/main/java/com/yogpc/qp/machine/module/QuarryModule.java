package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public interface QuarryModule {
    Identifier moduleId();

    enum Constant implements QuarryModule {
        DUMMY,
        PUMP,
        BEDROCK,
        ;

        Constant() {
        }

        @Override
        public Identifier moduleId() {
            return Identifier.fromNamespaceAndPath(QuarryPlus.modID, name().toLowerCase(Locale.ROOT) + "_module");
        }
    }
}
