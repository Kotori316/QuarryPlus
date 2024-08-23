package com.yogpc.qp.common.data

import com.mojang.serialization.Lifecycle
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment
import net.minecraft.core.RegistrySetBuilder.RegistryBootstrap
import net.minecraft.core.registries.Registries
import net.minecraft.core.{HolderLookup, RegistrySetBuilder}
import net.minecraft.data.PackOutput
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlotGroup
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

  private class Registerer extends RegistryBootstrap[Enchantment] {
    override def run(context: BootstrapContext[Enchantment]): Unit = {
      val enchantment = Enchantment.enchantment(
        Enchantment.definition(
          context.lookup(Registries.ITEM).getOrThrow(quarryPickaxeTag),
          1,
          1,
          Enchantment.constantCost(25), Enchantment.constantCost(50),
          1,
          EquipmentSlotGroup.HAND
        )
      ).build(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, QuarryPickaxeEnchantment.NAME))
      context.register(QuarryPickaxeEnchantment.KEY, enchantment)
    }
  }
}
