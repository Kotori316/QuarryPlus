package com.yogpc.qp

import java.nio.ByteBuffer
import java.util
import java.util.stream
import java.util.stream.{IntStream, LongStream}

import com.google.gson.JsonElement
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.{DataResult, DynamicOps, JsonOps}
import net.minecraft.nbt.{NbtOps, Tag}

import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters._

object ScalaRawOps extends DynamicOps[Any] {

  override val empty: Object = new Object

  override def convertTo[U](outOps: DynamicOps[U], input: Any): U = input match {
    case null | ScalaRawOps.empty => outOps.empty()
    case map: Map[_, _] => convertMap(outOps, map)
    case intArray@Array(_: Int, _*) => outOps.createIntList(intArray.map(_.asInstanceOf[Int]).asJavaSeqStream)
    case byteArray@Array(_: Byte, _*) => outOps.createByteList(ByteBuffer.wrap(byteArray.map(_.asInstanceOf[Byte])))
    case longArray@Array(_: Long, _*) => outOps.createLongList(longArray.map(_.asInstanceOf[Long]).asJavaSeqStream)
    case byteArray: Iterable[_] if byteArray.headOption.exists(_.isInstanceOf[Byte]) => outOps.createByteList(ByteBuffer.wrap(byteArray.map(_.asInstanceOf[Byte]).toArray))
    case intArray: Iterable[_] if intArray.headOption.exists(_.isInstanceOf[Int]) => outOps.createIntList(intArray.map(_.asInstanceOf[Int]).toArray.asJavaSeqStream)
    case longArray: Iterable[_] if longArray.headOption.exists(_.isInstanceOf[Long]) => outOps.createLongList(longArray.map(_.asInstanceOf[Long]).toArray.asJavaSeqStream)
    case iterable: Iterable[_] => convertList(outOps, iterable)
    case string: String => outOps.createString(string)
    case boolean: Boolean => outOps.createBoolean(boolean)
    case aInt: Int => outOps.createInt(aInt)
    case aByte: Byte => outOps.createByte(aByte)
    case aShort: Short => outOps.createShort(aShort)
    case aLong: Long => outOps.createLong(aLong)
    case aFloat: Float => outOps.createFloat(aFloat)
    case aDouble: Double => outOps.createDouble(aDouble)
    case number: Number => outOps.createNumeric(number)
  }

  override def getNumberValue(input: Any): DataResult[Number] = input match {
    case number: Number => DataResult.success(number)
    case _ => DataResult.error("Not a number: " + input)
  }

  override def createNumeric(i: Number): Number = i

  override def createByte(value: Byte): Byte = value

  override def createInt(value: Int): Int = value

  override def createLong(value: Long): Long = value

  override def createFloat(value: Float): Float = value

  override def createDouble(value: Double): Double = value

  override def createBoolean(value: Boolean): Boolean = value

  override def createMap(map: util.Map[Any, Any]): Map[Any, Any] = map.asScala.toMap

  override def createByteList(input: ByteBuffer): Array[Byte] = input.array()

  override def createIntList(input: IntStream): Array[Int] = input.toArray

  override def createLongList(input: LongStream): Array[Long] = input.toArray

  override def getStringValue(input: Any): DataResult[String] = DataResult.success(String.valueOf(input))

  override def createString(value: String): String = value

  override def mergeToList(list: Any, value: Any): DataResult[Any] = list match {
    case iterable: Iterable[_] => DataResult.success(iterable ++ Iterable.single(value))
    case null | ScalaRawOps.empty => DataResult.success(Seq(value))
    case _ => DataResult.error("mergeToList called with not a list: " + list, list)
  }

  override def mergeToList(list: Any, values: util.List[Any]): DataResult[Any] = list match {
    case iterable: Iterable[_] => DataResult.success(iterable ++ values.asScala)
    case null | ScalaRawOps.empty => DataResult.success(values.asScala)
    case _ => DataResult.error("mergeToList called with not a list: " + list, list);
  }

  override def mergeToMap(map: Any, key: Any, value: Any): DataResult[Any] = map match {
    case m: Map[_, _] => DataResult.success(m.asInstanceOf[Map[Any, Any]].updated(key, value))
    case null | ScalaRawOps.empty => DataResult.success(Map(key -> value))
    case _ => DataResult.error("mergeToMap called with not a map: " + map, map)
  }

  override def getMapValues(input: Any): DataResult[stream.Stream[Pair[Any, Any]]] = input match {
    case map: Map[_, _] => DataResult.success(map.map { case (k, v) => Pair.of[Any, Any](k, v) }.asJavaSeqStream)
    case null | ScalaRawOps.empty => DataResult.success(stream.Stream.empty())
    case _ => DataResult.error("Not a map: " + input)
  }

  override def createMap(map: stream.Stream[Pair[Any, Any]]): Any =
    map.toScala(Seq).map(p => p.getFirst -> p.getSecond).toMap

  override def getStream(input: Any): DataResult[stream.Stream[Any]] = input match {
    case iterable: Iterable[Any] => DataResult.success(iterable.asJavaSeqStream)
    case _ => DataResult.error("Not an iterable: " + input)
  }

  override def createList(input: stream.Stream[Any]): LazyList[Any] = input.toScala(LazyList)

  override def remove(input: Any, key: String): Any = input match {
    case map: Map[_, _] if map.keySet.asInstanceOf[Set[String]].contains(key) => map.asInstanceOf[Map[Any, Any]].removed(key)
    case _ => input
  }

  override def toString: String = "ScalaObject"

  implicit final class ToNbtConverter(private val value: Any) extends AnyVal {
    def toNbt: Tag = ScalaRawOps.convertTo(NbtOps.INSTANCE, value)

    def toJson: JsonElement = ScalaRawOps.convertTo(JsonOps.INSTANCE, value)
  }
}
