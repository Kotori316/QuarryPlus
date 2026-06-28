package com.yogpc.qp.common.data

import com.yogpc.qp.{PlatformAccess, QuarryPlus}
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.references.ItemIds
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.common.data.ItemTagsProvider

import java.util.concurrent.CompletableFuture

class QuarryItemTagProvider(output: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider])
  extends ItemTagsProvider(output, lookupProvider, QuarryPlus.modID) {

  override def addTags(provider: HolderLookup.Provider): Unit = {
    val holder = PlatformAccess.getAccess.registerObjects()
    tag(markerItemTag)
      .add(ResourceKey.create(Registries.ITEM, holder.markerBlock().get().name))
      .add(ResourceKey.create(Registries.ITEM, holder.flexibleMarkerBlock().get().name))
      .add(ResourceKey.create(Registries.ITEM, holder.chunkMarkerBlock().get().name))
    tag(quarryPickaxeTag).add(ItemIds.DIAMOND_PICKAXE, ItemIds.NETHERITE_PICKAXE)
  }
}
