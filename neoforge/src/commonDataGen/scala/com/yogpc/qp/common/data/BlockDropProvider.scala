package com.yogpc.qp.common.data

import com.yogpc.qp.machine.MachineLootFunction
import com.yogpc.qp.{PlatformAccess, QuarryPlus}
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.Block

import scala.jdk.CollectionConverters.{IterableHasAsJava, IterableHasAsScala}

class BlockDropProvider(registries: HolderLookup.Provider) extends BlockLootSubProvider(
  java.util.Collections.emptySet(), FeatureFlags.DEFAULT_FLAGS, registries
) {

  override def generate(): Unit = {
    val holder = PlatformAccess.getAccess.registerObjects()
    Seq(
      holder.moverBlock().get(),
      holder.markerBlock().get(),
      holder.generatorBlock().get()
    ).foreach(this.dropSelf)

    Seq(
      holder.quarryBlock().get()
    ).foreach(b => add(b, createSingleItemTable(b).apply(MachineLootFunction.builder())))
  }

  override def getKnownBlocks: java.lang.Iterable[Block] = {
    BuiltInRegistries.BLOCK.entrySet().asScala
      .filter(_.getKey.location().getNamespace == QuarryPlus.modID)
      .map(_.getValue)
      .asJava
  }
}
