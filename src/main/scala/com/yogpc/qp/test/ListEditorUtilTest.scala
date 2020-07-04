package com.yogpc.qp.test

import cats._
import cats.implicits._
import com.yogpc.qp.machines.item.ItemListEditor._
import net.minecraft.enchantment.{EnchantmentHelper, Enchantments}
import net.minecraft.item.{ItemStack, Items}
import net.minecraftforge.registries.ForgeRegistries
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import scala.jdk.CollectionConverters._
import scala.util.Random
import scala.util.chaining._

class ListEditorUtilTest {
  @Test
  def onlySilktouchTest(): Unit = {
    val silk = new ItemStack(Items.DIAMOND_PICKAXE).tap(_.addEnchantment(Enchantments.SILK_TOUCH, 1))
    val silkAndEff = silk.copy().tap(_.addEnchantment(Enchantments.EFFICIENCY, 5))
    val fortune = new ItemStack(Items.DIAMOND_PICKAXE).tap(_.addEnchantment(Enchantments.FORTUNE, 3))

    assertAll(Seq((silk, true), (silkAndEff, false), (fortune, false)).map[Executable] { case (stack, bool) => () =>
      assertTrue(onlySilktouch(stack) === Eval.now(bool),
        s"Only silktouch check of $stack.")
    }: _*)
  }

  @Test
  def onlySpecificTest(): Unit = {
    val rand = new Random()
    val purePickaxes = ForgeRegistries.ENCHANTMENTS.asScala.map { e =>
      new ItemStack(Items.DIAMOND_PICKAXE)
        .tap(_.addEnchantment(e, 1))
    }.toSeq
    val addedPickaxes = purePickaxes.map(i => EnchantmentHelper.addRandomEnchantment(rand.self, i.copy(), 30, true))

    val executables = (purePickaxes zip addedPickaxes zip ForgeRegistries.ENCHANTMENTS.asScala).flatMap { case ((pure, added), enchantment) =>
      val checker = onlySpecificEnchantment(Eval.now(enchantment))
      Seq.apply[Executable](
        () => assertTrue(checker(pure) === Eval.True, s"Pure $pure has only $enchantment."),
      ) ++ Option(added).filter(i => EnchantmentHelper.getEnchantments(i).size() > 1).map[Executable](i =>
        () => assertTrue(checker(i) === Eval.False, f"$i has ${EnchantmentHelper.getEnchantments(i)}.")
      )
    }

    assertAll(executables: _*)
  }

  @Test
  def hasFortuneTest(): Unit = {
    val fortune = new ItemStack(Items.DIAMOND_PICKAXE).tap(_.addEnchantment(Enchantments.FORTUNE, 3))
    val fortuneAndEff = fortune.copy().tap(_.addEnchantment(Enchantments.EFFICIENCY, 5))
    val silk = new ItemStack(Items.DIAMOND_PICKAXE).tap(_.addEnchantment(Enchantments.SILK_TOUCH, 1))

    assertAll(Seq((silk, false), (fortuneAndEff, true), (fortune, true)).map[Executable] { case (stack, bool) => () =>
      assertTrue(isFortune(stack) === Eval.now(bool),
        s"Has fortune check of $stack.")
    }: _*)
  }
}
