package com.yogpc.qp.data;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

@SuppressWarnings("unused")
public final class QuarryDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        try {
            QuarryPlus.LOGGER.info("{} Data Generator is called.", QuarryPlus.MOD_NAME);
            var clazz = Class.forName("com.yogpc.qp.data.Generator");
            var instance = (DataGeneratorEntrypoint) clazz.getConstructor().newInstance();
            instance.onInitializeDataGenerator(fabricDataGenerator);
        } catch (ReflectiveOperationException e) {
            QuarryPlus.LOGGER.error("Who calls %s data generator without test sources?".formatted(QuarryPlus.MOD_NAME), e);
        }
    }
}
