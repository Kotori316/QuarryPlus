package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.EnchantmentTagsProvider
import net.minecraft.tags.EnchantmentTags
import net.neoforged.neoforge.common.data.{DatapackBuiltinEntriesProvider, ExistingFileHelper}

class QuarryEnchantmentTagProvider(output: PackOutput, entryProvider: DatapackBuiltinEntriesProvider, existingFileHelper: ExistingFileHelper)
  extends EnchantmentTagsProvider(output, entryProvider.getRegistryProvider, QuarryPlus.modID, existingFileHelper) {

  override def addTags(provider: HolderLookup.Provider): Unit = {
    tag(EnchantmentTags.PREVENTS_INFESTED_SPAWNS).add(QuarryPickaxeEnchantment.KEY)
  }
}
