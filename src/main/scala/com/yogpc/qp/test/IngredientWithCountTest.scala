package com.yogpc.qp.test

import com.yogpc.qp.machines.workbench.IngredientWithCount
import io.netty.buffer.Unpooled
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.network.PacketBuffer
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import scala.jdk.javaapi.StreamConverters

private[test] object IngredientWithCountTest {
  @ParameterizedTest
  @MethodSource(Array("com.yogpc.qp.test.IngredientWithCountTest#stackList"))
  def jsonConsistency(stack: ItemStack): Unit = {
    val iwc = new IngredientWithCount(stack)
    val json = iwc.serializeJson.getAsJsonObject

    val deserialized = new IngredientWithCount(json)
    assertEquals(json, deserialized.serializeJson)
  }

  @ParameterizedTest
  @MethodSource(Array("com.yogpc.qp.test.IngredientWithCountTest#stackList"))
  def packetConsistency(stack: ItemStack): Unit = {
    val iwc = new IngredientWithCount(stack)
    val packet = new PacketBuffer(Unpooled.buffer())
    iwc.writeToBuffer(packet)

    val loaded = IngredientWithCount.readFromBuffer(packet)
    assertEquals(iwc.serializeJson, loaded.serializeJson)
  }

  def stackList(): java.util.stream.Stream[ItemStack] = StreamConverters.asJavaSeqStream(
    for {
      item <- Seq(Items.APPLE, Items.DIRT, Items.DIAMOND, Items.GOLDEN_PICKAXE, Items.CHAINMAIL_CHESTPLATE)
      count <- Seq(1, 10, 64, 512, 1024)
    } yield new ItemStack(item, count)
  )
}
