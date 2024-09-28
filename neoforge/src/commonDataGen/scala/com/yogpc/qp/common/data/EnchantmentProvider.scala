package com.yogpc.qp.common.data

import com.mojang.serialization.Lifecycle
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment
import net.minecraft.core.RegistrySetBuilder.RegistryBootstrap
import net.minecraft.core.registries.Registries
import net.minecraft.core.{HolderLookup, RegistrySetBuilder}
import net.minecraft.data.PackOutput
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.world.item.enchantment.Enchantment
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider

import java.util.Collections
import java.util.concurrent.CompletableFuture

class EnchantmentProvider(pack: PackOutput, lookupProvider: CompletableFuture[HolderLookup.Provider])
  extends DatapackBuiltinEntriesProvider(pack, lookupProvider, EnchantmentProvider.builder(), Collections.singleton(QuarryPlus.modID)) {

}

object EnchantmentProvider {
  def builder(): RegistrySetBuilder = {
    val builder = new RegistrySetBuilder()
    builder.add(Registries.ENCHANTMENT, Lifecycle.stable(), new Registerer)
    builder
  }

  private final class Registerer extends RegistryBootstrap[Enchantment] {
    override def run(context: BootstrapContext[Enchantment]): Unit = {
      val enchantment = QuarryPickaxeEnchantment.createEnchantment(context, quarryPickaxeTag)
      context.register(QuarryPickaxeEnchantment.KEY, enchantment)
    }
  }
}
