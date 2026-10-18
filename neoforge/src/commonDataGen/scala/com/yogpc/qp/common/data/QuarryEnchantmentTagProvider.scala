package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.EnchantmentTagsProvider
import net.minecraft.tags.EnchantmentTags

import java.util.concurrent.CompletableFuture

class QuarryEnchantmentTagProvider(output: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider])
  extends EnchantmentTagsProvider(output, lookupProvider, QuarryPlus.modID) {

  override def addTags(provider: HolderLookup.Provider): Unit = {
    tag(EnchantmentTags.PREVENTS_INFESTED_SPAWNS).add(QuarryPickaxeEnchantment.KEY)
  }
}
