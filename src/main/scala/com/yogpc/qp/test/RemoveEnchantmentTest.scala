package com.yogpc.qp.test

import com.yogpc.qp._
import com.yogpc.qp.utils.Holder
import net.minecraft.enchantment.{EnchantmentData, EnchantmentHelper, Enchantments}
import net.minecraft.item.{EnchantedBookItem, ItemStack, Items}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

//noinspection DuplicatedCode It's test!
private[test] final class RemoveEnchantmentTest {
  @Test def removeEnchantment1(): Unit = {
    val stack = new ItemStack(Holder.itemQuarryPickaxe)
    stack.addEnchantment(Enchantments.SILK_TOUCH, 1)
    assertEquals(1, EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack))
    assertTrue(stack.hasTag)

    stack.removeEnchantment(Enchantments.SILK_TOUCH)
    assertEquals(0, EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack))
  }

  @Test def removeEnchantment2(): Unit = {
    val stack = new ItemStack(Holder.itemQuarryPickaxe)
    stack.addEnchantment(Enchantments.SILK_TOUCH, 1)
    stack.addEnchantment(Enchantments.EFFICIENCY, 5)
    assertEquals(1, EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack))
    assertEquals(5, EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack))
    assertTrue(stack.hasTag)

    stack.removeEnchantment(Enchantments.SILK_TOUCH)
    assertEquals(5, EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack))
    assertEquals(0, EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack))
    assertTrue(stack.hasTag)
  }


  @Test def removeEnchantment3(): Unit = {
    val stack = new ItemStack(Holder.itemQuarryPickaxe)
    val tag = stack.getOrCreateTag()
    tag.putInt("int", 6)
    stack.setTag(tag)
    stack.addEnchantment(Enchantments.SILK_TOUCH, 1)
    stack.addEnchantment(Enchantments.EFFICIENCY, 5)
    assertEquals(1, EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack))
    assertEquals(5, EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack))
    assertTrue(stack.hasTag)

    stack.removeEnchantment(Enchantments.SILK_TOUCH)
    assertEquals(5, EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack))
    assertEquals(0, EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack))
    assertTrue(stack.hasTag)
    assertEquals(6, stack.getTag.getInt("int"))
  }

  @Test def removeBookEnchantment1(): Unit = {
    val book = new ItemStack(Items.ENCHANTED_BOOK)
    EnchantedBookItem.addEnchantment(book, new EnchantmentData(Enchantments.SILK_TOUCH, 1))
    val beforeMap = EnchantmentHelper.getEnchantments(book)
    assertEquals(Integer.valueOf(1), beforeMap.get(Enchantments.SILK_TOUCH))

    book.removeEnchantment(Enchantments.SILK_TOUCH)
    val newMap = EnchantmentHelper.getEnchantments(book)
    assertTrue(newMap.isEmpty)
    assertFalse(book.hasTag)
  }

  @Test def removeBookEnchantment2(): Unit = {
    val book = new ItemStack(Items.ENCHANTED_BOOK)
    EnchantedBookItem.addEnchantment(book, new EnchantmentData(Enchantments.SILK_TOUCH, 1))
    EnchantedBookItem.addEnchantment(book, new EnchantmentData(Enchantments.EFFICIENCY, 5))
    val beforeMap = EnchantmentHelper.getEnchantments(book)
    assertEquals(Integer.valueOf(1), beforeMap.get(Enchantments.SILK_TOUCH))
    assertEquals(Integer.valueOf(5), beforeMap.get(Enchantments.EFFICIENCY))

    book.removeEnchantment(Enchantments.SILK_TOUCH)
    val newMap = EnchantmentHelper.getEnchantments(book)
    assertEquals(null, newMap.get(Enchantments.SILK_TOUCH))
    assertEquals(Integer.valueOf(5), newMap.get(Enchantments.EFFICIENCY))
    assertFalse(newMap.isEmpty)
    assertTrue(book.hasTag)
  }
}
