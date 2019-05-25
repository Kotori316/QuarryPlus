package com.yogpc.qp.utils

import java.lang.reflect.Type

import com.google.gson._
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.JsonUtils

import scala.util.Try

/**
  *
  * @param state          the state
  * @param ignoreProperty whether distinguish north-faced chest from south-faced chest.
  */
case class BlockWrapper(state: IBlockState,
                        ignoreProperty: Boolean = false)
  extends java.util.function.Predicate[IBlockState] {

  def apply(v1: IBlockState): Boolean = contain(v1)

  def contain(that: IBlockState): Boolean = {
     if (ignoreProperty) {
      state.getBlock == that.getBlock
    } else {
      state == that
    }
  }

  override def test(t: IBlockState): Boolean = contain(t)
}

object BlockWrapper extends JsonDeserializer[BlockWrapper] with JsonSerializer[BlockWrapper] {

  import com.yogpc.qp._

  private[this] val GSON = (new GsonBuilder).setPrettyPrinting().disableHtmlEscaping()
    .registerTypeAdapter(classOf[BlockWrapper], this)
    .create()
  final val KEY_STATE = "blockstate"
  final val KEY_Property = "ignoreProperty"

  def getString(seq: Seq[BlockWrapper]): String = {
    GSON.toJson(seq.toArray, classOf[Array[BlockWrapper]])
  }

  def getWrapper(s: String): Set[BlockWrapper] = {
    import scala.collection.JavaConverters._
    val value = GSON.fromJson(s, classOf[JsonElement])
    if (value.isJsonArray) {
      value.getAsJsonArray.asScala.map(GSON.fromJson(_, classOf[BlockWrapper])).toSet
    } else {
      Set(GSON.fromJson(value.getAsJsonObject, classOf[BlockWrapper]))
    }
  }

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BlockWrapper = {
    val maybeWrapper = for (state <- NBTBuilder.getStateFromJson(JsonUtils.getJsonObject(json.getAsJsonObject, KEY_STATE)).asScala;
                            property <- Try(JsonUtils.getBoolean(json.getAsJsonObject, KEY_Property, false)).toOption)
      yield BlockWrapper(state, ignoreProperty = property)
    maybeWrapper.getOrElse(BlockWrapper(Blocks.AIR.getDefaultState))
  }

  override def serialize(src: BlockWrapper, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val obj = new JsonObject
    obj.add(KEY_STATE, NBTBuilder.fromBlockState(src.state))
    obj.addProperty(KEY_Property, src.ignoreProperty)
    obj
  }
}