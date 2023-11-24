package com.yogpc.qp.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Accessed from {@link QuarryDataGenerator} via reflection.
 */
@SuppressWarnings("unused")
public final class Generator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(RecipeGenerator::new);
        pack.addProvider(TagGenerator::new);
    }
}
