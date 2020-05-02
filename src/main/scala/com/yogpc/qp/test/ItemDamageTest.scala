package com.yogpc.qp.test

import cats.implicits._
import com.google.gson.JsonObject
import com.yogpc.qp.utils.{Holder, ItemDamage}
import net.minecraft.item.{ItemStack, Items}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ItemDamageTest {

  @Test
  def createInstance(): Unit = {
    assertEquals(ItemDamage.invalid, ItemDamage(ItemStack.EMPTY))
    assertTrue(ItemDamage.invalid === ItemDamage(ItemStack.EMPTY))

    val stack = new ItemStack(Holder.blockPlainPipe, 4)
    val d1 = ItemDamage(stack)
    val d2 = ItemDamage(stack)
    assertAll(
      () => assertTrue(d1 === d2),
      () => assertNotEquals(ItemDamage.invalid, d1),
      () => assertNotEquals(ItemDamage.invalid, d2),
      () => assertFalse(ItemDamage.invalid === d1),
    )
    assertTrue(ItemStack.areItemStacksEqual(stack, d1.toStack(4)))
  }

  @Test
  def serializeJsonTest(): Unit = {
    val d = ItemDamage(new ItemStack(Items.APPLE))
    val j = d.serializeJson
    val expected = new JsonObject
    locally {
      expected.addProperty("item", "minecraft:apple")
    }
    assertEquals(expected, j)
  }
}
