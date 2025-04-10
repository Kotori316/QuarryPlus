package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.data.SpriteSourceProvider

import java.util.concurrent.CompletableFuture

class QuarrySpriteSourceProvider(output: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider])
  extends SpriteSourceProvider(output, lookupProvider, QuarryPlus.modID) {

  override def gather(): Unit = {
    atlas(SpriteSourceProvider.BLOCKS_ATLAS)
      .addSource(DirectoryLister("entity/quarry", "entity/"))
  }
}
