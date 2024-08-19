package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.{ItemTagsProvider, TagsProvider}
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.data.ExistingFileHelper

import java.util.concurrent.CompletableFuture

class QuarryItemTagProvider(output: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider], existingFileHelper: ExistingFileHelper, blockTags: CompletableFuture[TagsProvider.TagLookup[Block]])
  extends ItemTagsProvider(output, lookupProvider, blockTags, QuarryPlus.modID, existingFileHelper) {

  override def addTags(provider: HolderLookup.Provider): Unit = {
    copy(markerBlockTag, markerItemTag)
  }
}
