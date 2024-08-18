package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.{ExistingFileHelper, SpriteSourceProvider}

import java.util.concurrent.CompletableFuture

class QuarrySpriteSourceProvider(output: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider], existingFileHelper: ExistingFileHelper)
  extends SpriteSourceProvider(output, lookupProvider, QuarryPlus.modID, existingFileHelper) {

  override def gather(): Unit = {
    atlas(SpriteSourceProvider.BLOCKS_ATLAS)
      .addSource(DirectoryLister("entity/quarry", "entity/"))
  }
}
