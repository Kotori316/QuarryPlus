package com.yogpc.qp.machines.advquarry

import java.lang.reflect.{GenericArrayType, Type}

import com.google.gson._
import com.mojang.serialization.JsonOps
import com.yogpc.qp.utils.JsonReloadListener
import net.minecraft.block.{Block, BlockState, Blocks}
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraft.tags.{BlockTags, ITag, TagCollectionManager}
import net.minecraft.util.{JSONUtils, ResourceLocation}
import net.minecraftforge.common.Tags

import scala.jdk.CollectionConverters._
import scala.jdk.FunctionConverters._
import scala.util.Try

abstract class BlockWrapper(val name: String)
  extends java.util.function.Predicate[BlockState] {

  def apply(v1: BlockState): Boolean = contain(v1)

  def contain(that: BlockState): Boolean

  override def test(t: BlockState): Boolean = contain(t)

  def serialize(obj: JsonObject, typeOfSrc: Type, context: JsonSerializationContext): JsonElement
}

object BlockWrapper extends JsonDeserializer[BlockWrapper] with JsonSerializer[BlockWrapper] {
  def apply(state: BlockState, ignoreProperty: Boolean = false): BlockWrapper = new State(state, ignoreProperty)

  def apply(tag: ITag[Block]): BlockWrapper = new TagPredicate(tag)

  val example: Array[BlockWrapper] = Array(
    BlockWrapper(Tags.Blocks.STONE),
    BlockWrapper(Tags.Blocks.COBBLESTONE),
    BlockWrapper(Blocks.DIRT.getDefaultState, ignoreProperty = true),
    BlockWrapper(Blocks.GRASS_BLOCK.getDefaultState, ignoreProperty = true),
    BlockWrapper(Blocks.NETHERRACK.getDefaultState),
    BlockWrapper(Tags.Blocks.SANDSTONE),
  )

  import com.yogpc.qp._

  private var wrappers = Set.empty[BlockWrapper]

  def getWrappers: Set[BlockWrapper] = wrappers

  private[this] final val arrayDeserializer = new JsonDeserializer[Array[BlockWrapper]] {
    override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Array[BlockWrapper] = {
      val componentType = typeOfT.asInstanceOf[GenericArrayType].getGenericComponentType
      if (json.isJsonArray) json.getAsJsonArray.iterator().asScala.map(j => context.deserialize[BlockWrapper](j, componentType)).toArray
      else Array(context.deserialize[BlockWrapper](json, componentType))
    }
  }

  final val GSON = (new GsonBuilder).setPrettyPrinting().disableHtmlEscaping()
    .registerTypeHierarchyAdapter(classOf[BlockWrapper], this)
    .registerTypeHierarchyAdapter(classOf[Array[BlockWrapper]], arrayDeserializer)
    .create()
  private[this] final val KEY_NAME = "name"
  private[this] final val KEY_STATE = "blockstate"
  private[this] final val KEY_Property = "ignoreProperty"
  private[this] final val KEY_Tag = "tag"

  private[this] final val NAME_NoMatch = QuarryPlus.modID + ":wrapper_none"
  private[this] final val NAME_State = QuarryPlus.modID + ":wrapper_state"
  private[this] final val NAME_Tag = QuarryPlus.modID + ":wrapper_tag"

  private object NoMatch extends BlockWrapper(NAME_NoMatch) {
    override def contain(that: BlockState) = false

    override def serialize(obj: JsonObject, typeOfSrc: Type, context: JsonSerializationContext) = obj
  }

  private class State(state: BlockState, ignoreProperty: Boolean = false) extends BlockWrapper(NAME_State) {
    def contain(that: BlockState): Boolean = {
      if (ignoreProperty) {
        state.getBlock == that.getBlock
      } else {
        state == that
      }
    }

    override def serialize(obj: JsonObject, typeOfSrc: Type, context: JsonSerializationContext) = {
      obj.addProperty(KEY_NAME, name)
      val f = (s: String) => QuarryPlus.LOGGER.error(s)
      obj.add(KEY_STATE, BlockState.field_235877_b_.encodeStart(JsonOps.INSTANCE, state).getOrThrow(false, f.asJavaConsumer))
      obj.addProperty(KEY_Property, ignoreProperty)
      obj
    }
  }

  private class TagPredicate(tag: ITag[Block]) extends BlockWrapper(NAME_Tag) {
    override def contain(that: BlockState) = tag.func_230235_a_(that.getBlock)

    override def serialize(obj: JsonObject, typeOfSrc: Type, context: JsonSerializationContext) = {
      obj.addProperty(KEY_NAME, name)
      obj.addProperty(KEY_Tag, TagCollectionManager.func_232928_e_().func_232923_a_().func_232975_b_(tag).toString)
      obj
    }
  }

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BlockWrapper = {
    val jsonObj = JSONUtils.getJsonObject(json, "wrapper")
    val name = JSONUtils.getString(jsonObj, KEY_NAME, NAME_NoMatch)
    name match {
      case NAME_State =>
        val maybeWrapper = for (result <- Try(BlockState.field_235877_b_.decode(JsonOps.INSTANCE, JSONUtils.getJsonObject(json.getAsJsonObject, KEY_STATE)));
                                state <- Try(result.result().get().getFirst);
                                property <- Try(JSONUtils.getBoolean(json.getAsJsonObject, KEY_Property, false)))
          yield new State(state, property)
        maybeWrapper.getOrElse(new State(Blocks.AIR.getDefaultState))
      case NAME_Tag =>
        val tagName = JSONUtils.getString(jsonObj, KEY_Tag)
        Option(BlockTags.getCollection.get(new ResourceLocation(tagName))).map(new TagPredicate(_)).getOrElse(NoMatch)
      case _ => NoMatch
    }
  }

  override def serialize(src: BlockWrapper, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
    src.serialize(new JsonObject, typeOfSrc, context)

  object Reload extends JsonReloadListener(BlockWrapper.GSON, QuarryPlus.modID + "/adv_quarry") {
    override def apply(splashList: java.util.Map[ResourceLocation, JsonElement], resourceManagerIn: IResourceManager, profilerIn: IProfiler): Unit = {
      BlockWrapper.wrappers = splashList.asScala.toSeq.collect { case (_, j) => GSON.fromJson(j, classOf[Array[BlockWrapper]]) }.flatten.toSet
      QuarryPlus.LOGGER.debug("Loaded BlockWrapper, {} objects.", BlockWrapper.wrappers.size.toString)
    }
  }

}
