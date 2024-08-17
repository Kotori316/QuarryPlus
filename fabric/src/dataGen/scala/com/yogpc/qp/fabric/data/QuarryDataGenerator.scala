package com.yogpc.qp.fabric.data

import com.yogpc.qp.data.Recipe
import net.fabricmc.fabric.api.datagen.v1.{DataGeneratorEntrypoint, FabricDataGenerator}

final class QuarryDataGenerator extends DataGeneratorEntrypoint {
  override def onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator): Unit = {
    val pack = fabricDataGenerator.createPack()
    pack.addProvider((o, r) => new Recipe(o, r))
  }
}
