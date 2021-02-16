package com.yogpc.qp.test

import com.yogpc.qp.machines.base.EnchantmentFilter
import com.yogpc.qp.machines.base.QuarryBlackList.Name
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.math.BlockPos
import net.minecraft.world.EmptyBlockReader
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

//noinspection DuplicatedCode
private[test] final class EnchantmentFilterTest {
  private def p(e: EnchantmentFilter, s: BlockState) = e.getEnchantmentPredicate(s, EmptyBlockReader.INSTANCE, BlockPos.ZERO)

  @Test
  def fortuneIncludeTest(): Unit = {
    val fortuneInclude = EnchantmentFilter.defaultInstance.copy(fortuneInclude = true, fortuneList = Set(Name(Blocks.DIAMOND_ORE.getRegistryName)))
    val diamond = p(fortuneInclude, Blocks.DIAMOND_ORE.getDefaultState)
    assertAll(
      () => assertTrue(diamond.test(Enchantments.FORTUNE), "Fortune for Diamond ore"),
      () => assertTrue(diamond.test(Enchantments.SILK_TOUCH), "Silktouch for Diamond ore"),
    )
    val coal = p(fortuneInclude, Blocks.COAL_ORE.getDefaultState)
    assertAll(
      () => assertFalse(coal.test(Enchantments.FORTUNE), "Fortune for Coal Ore"),
      () => assertTrue(coal.test(Enchantments.SILK_TOUCH), "Silktouch for Coal Ore"),
    )
  }

  @Test
  def silktouchIncludeTest(): Unit = {
    val silktouchInclude = EnchantmentFilter.defaultInstance.copy(silktouchInclude = true, silktouchList = Set(Name(Blocks.DIAMOND_ORE.getRegistryName)))
    val diamond = p(silktouchInclude, Blocks.DIAMOND_ORE.getDefaultState)
    assertAll(
      () => assertTrue(diamond.test(Enchantments.FORTUNE), "Fortune for Diamond ore"),
      () => assertTrue(diamond.test(Enchantments.SILK_TOUCH), "Silktouch for Diamond ore"),
    )
    val coal = p(silktouchInclude, Blocks.COAL_ORE.getDefaultState)
    assertAll(
      () => assertTrue(coal.test(Enchantments.FORTUNE), "Fortune for Coal Ore"),
      () => assertFalse(coal.test(Enchantments.SILK_TOUCH), "Silktouch for Coal Ore"),
    )
  }

  @Test
  def fortuneExclude(): Unit = {
    val block = Blocks.GLASS
    val fortuneExclude = EnchantmentFilter.defaultInstance.copy(fortuneList = Set(Name(block.getRegistryName)))
    val glass = p(fortuneExclude, block.getDefaultState)
    assertAll(
      () => assertFalse(glass.test(Enchantments.FORTUNE), s"Fortune for $block"),
      () => assertTrue(glass.test(Enchantments.SILK_TOUCH), s"Silktouch for $block"),
    )
    val stone = p(fortuneExclude, Blocks.STONE.getDefaultState)
    assertAll(
      () => assertTrue(stone.test(Enchantments.FORTUNE), "Fortune for Stone"),
      () => assertTrue(stone.test(Enchantments.SILK_TOUCH), "Silktouch for Stone"),
    )
  }

  @Test
  def defaultExclude(): Unit = {
    val filter = EnchantmentFilter.defaultInstance
    val excludeAll = filter.copy(fortuneInclude = true, silktouchInclude = true)
    val blockList = List(Blocks.SAND, Blocks.AIR, Blocks.STONE, Blocks.COAL_ORE, Blocks.DIAMOND_ORE)
    for (block <- blockList) {
      val predicate1 = p(filter, block.getDefaultState)
      val predicate2 = p(excludeAll, block.getDefaultState)
      assertAll(
        () => assertTrue(predicate1.test(Enchantments.FORTUNE), s"Include Fortune for $block"),
        () => assertTrue(predicate1.test(Enchantments.SILK_TOUCH), s"Include Silktouch for $block"),
        () => assertFalse(predicate2.test(Enchantments.FORTUNE), s"Exclude Fortune for $block"),
        () => assertFalse(predicate2.test(Enchantments.SILK_TOUCH), s"Exclude Silktouch for $block"),
      )
    }
  }
}
