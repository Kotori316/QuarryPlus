package com.yogpc.qp.data

import com.google.gson.JsonObject
import com.yogpc.qp.{Holder, QuarryPlus}
import net.minecraft.data.DataGenerator
import net.minecraft.resources.ResourceLocation

import java.util.Collections
import scala.jdk.javaapi.CollectionConverters

class DefaultMachineConfig(generator: DataGenerator)
  extends QuarryDataProvider(generator) {
  override def directory(): String = "../.." // To save to root dir

  override def data(): java.util.List[_ <: DataBuilder] = {
    Collections.singletonList(DefaultMachineConfig)
  }
}

private object DefaultMachineConfig extends DataBuilder {
  override def location(): ResourceLocation = new ResourceLocation(QuarryPlus.modID, "machine_default")

  override def build(): JsonObject = {
    CollectionConverters.asScala(Holder.conditionHolders)
      .filter(_.configurable)
      .sortBy(_.path)
      .map(h => h.path -> h.condition.name())
      .foldLeft(new JsonObject) { case (o, (key, value)) =>
        o.addProperty(key, value)
        o
      }
  }
}
