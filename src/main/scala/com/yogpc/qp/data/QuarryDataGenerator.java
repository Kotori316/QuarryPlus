package com.yogpc.qp.data;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

@SuppressWarnings("unused")
public final class QuarryDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        try {
            QuarryPlus.LOGGER.info("Quarry Data Generator is called.");
            var clazz = Class.forName("com.yogpc.qp.data.Generator");
            var instance = (DataGeneratorEntrypoint) clazz.getConstructor().newInstance();
            instance.onInitializeDataGenerator(fabricDataGenerator);
        } catch (ReflectiveOperationException ignore) {
        }
    }
}
