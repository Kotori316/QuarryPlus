package com.yogpc.qp.utils

import com.yogpc.qp.QuarryPlusTest
import com.yogpc.qp.ScalaRawOps._
import net.minecraft.nbt._
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.{DynamicTest, Test, TestFactory}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

@ExtendWith(Array(classOf[QuarryPlusTest]))
class RawValueConverterTest {
  @Test
  def numbers(): Unit = {
    assertAll(
      () => assertEquals(IntTag.valueOf(1), 1.toNbt),
      () => assertEquals(ShortTag.valueOf(1), 1.toShort.toNbt),
      () => assertEquals(ByteTag.valueOf(1.toByte), 1.toByte.toNbt),
      () => assertEquals(LongTag.valueOf(1), 1L.toNbt),
      () => assertEquals(DoubleTag.valueOf(1d), 1d.toNbt),
      () => assertEquals(FloatTag.valueOf(1f), 1.toFloat.toNbt),
    )
  }

  @TestFactory
  def intArray() = {
    val expected = new IntArrayTag(Range(0, 10).toArray)

    Seq(
      DynamicTest.dynamicTest("Range", () => assertEquals(expected, Range(0, 10).toNbt)),
      DynamicTest.dynamicTest("List", () => assertEquals(expected, List.range(0, 10).toNbt)),
      DynamicTest.dynamicTest("Int Array", () => assertEquals(expected, Array.range(0, 10).toNbt)),
    ).asJava
  }

  @TestFactory
  def longArray() = {
    val longs = Array(-1L, 0L, 15L, 43L, Long.MaxValue, Long.MinValue, Int.MaxValue + 4L, Int.MinValue - 4L)
    val expected = new LongArrayTag(longs)
    Seq(
      DynamicTest.dynamicTest("Long Array", () => assertEquals(expected, longs.toNbt)),
      DynamicTest.dynamicTest("Seq", () => assertEquals(expected, Seq(-1L, 0L, 15L, 43L, Long.MaxValue, Long.MinValue, Int.MaxValue + 4L, Int.MinValue - 4L).toNbt)),
    ).asJava
  }

  @Test
  def map1(): Unit = {
    val map: Map[String, Any] = Map(
      "key1" -> Map(
        "sub1" -> "a",
        "sub2" -> 4
      ),
      "key2" -> 4d,
      "key3" -> Seq("a", "b", "c"),
      "key4" -> null
    )
    val expected = new CompoundTag().tap { c =>
      c.put("key1", new CompoundTag().tap { cc =>
        cc.putString("sub1", "a")
        cc.putInt("sub2", 4)
      })
      c.putDouble("key2", 4d)
      c.put("key3", new ListTag().tap { l => Seq("a", "b", "c").map(StringTag.valueOf).foreach(l.add) })
      c.put("key4", EndTag.INSTANCE)
    }
    assertEquals(expected, map.toNbt)
  }
}
