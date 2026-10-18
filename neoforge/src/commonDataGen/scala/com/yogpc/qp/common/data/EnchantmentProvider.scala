package com.yogpc.qp.common.data

import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment
import net.minecraft.core.registries.SingleRegistryBootstrap
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.world.item.enchantment.Enchantment

object EnchantmentProvider extends SingleRegistryBootstrap[Enchantment] {
  override def run(context: BootstrapContext[Enchantment]): Unit = {
    val enchantment = QuarryPickaxeEnchantment.createEnchantment(context, quarryPickaxeTag)
    context.register(QuarryPickaxeEnchantment.KEY, enchantment)
  }
}
