package com.yogpc.qp.common.data

import com.yogpc.qp.{PlatformAccess, QuarryPlus}
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.world.item.Items
import net.neoforged.neoforge.common.data.ItemTagsProvider

import java.util.concurrent.CompletableFuture

class QuarryItemTagProvider(output: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider])
  extends ItemTagsProvider(output, lookupProvider, QuarryPlus.modID) {

  override def addTags(provider: HolderLookup.Provider): Unit = {
    val holder = PlatformAccess.getAccess.registerObjects()
    tag(markerItemTag)
      .add(holder.markerBlock().get().blockItem)
      .add(holder.flexibleMarkerBlock().get().blockItem)
      .add(holder.chunkMarkerBlock().get().blockItem)
    tag(quarryPickaxeTag).add(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)
  }
}
