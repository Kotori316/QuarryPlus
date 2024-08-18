package com.yogpc.qp.fabric.data

import net.fabricmc.fabric.api.datagen.v1.{DataGeneratorEntrypoint, FabricDataGenerator}

final class QuarryDataGenerator extends DataGeneratorEntrypoint {
  override def onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator): Unit = {
    val pack = fabricDataGenerator.createPack()
    pack.addProvider((o, r) => new RecipeFabric(o, r))
  }
}
