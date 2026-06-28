package com.yogpc.qp.common.data

import com.yogpc.qp.{PlatformAccess, QuarryPlus}
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.BlockTagsProvider

import java.util.concurrent.CompletableFuture

class QuarryBlockTagProvider(output: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider])
  extends BlockTagsProvider(output, lookupProvider, QuarryPlus.modID) {

  override def addTags(provider: HolderLookup.Provider): Unit = {
    val holder = PlatformAccess.getAccess.registerObjects()

    tag(BlockTags.MINEABLE_WITH_PICKAXE)
      .add(ResourceKey.create(Registries.BLOCK, holder.quarryBlock().get().name))
      .add(ResourceKey.create(Registries.BLOCK, holder.advQuarryBlock().get().name))

    tag(markerBlockTag)
      .add(ResourceKey.create(Registries.BLOCK, holder.markerBlock().get().name))
      .add(ResourceKey.create(Registries.BLOCK, holder.flexibleMarkerBlock().get().name))
      .add(ResourceKey.create(Registries.BLOCK, holder.chunkMarkerBlock().get().name))
  }
}
