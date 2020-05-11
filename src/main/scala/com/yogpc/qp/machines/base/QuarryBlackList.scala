package com.yogpc.qp.machines.base

import java.lang.reflect.{GenericArrayType, Type}
import java.util

import com.google.gson._
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.utils.JsonReloadListener
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{JSONUtils, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.common.Tags
import org.apache.logging.log4j.LogManager

import scala.jdk.javaapi.CollectionConverters

object QuarryBlackList {
  private final val LOGGER = LogManager.getLogger(getClass)

  /**
   * Check the block is registered in quarry blacklist.
   * The entry of blacklist will NOT be mined by machines in this mod.
   *
   * @param state the state representing the block.
   * @param world the world where block exists.
   * @param pos   the position where block exists.
   * @return if the block represented by `state` is registered as blacklist, `true`.
   */
  def contains(state: BlockState, world: World, pos: BlockPos): Boolean = entries.exists(_.test(state, world, pos))

  final val example: Array[Entry] = Array(Air)

  private var entries: Set[Entry] = Set.empty

  abstract class Entry(val id: String) {
    def test(state: BlockState, world: World, pos: BlockPos): Boolean
  }

  private[this] final val ID_AIR = QuarryPlus.modID + ":blacklist_air"
  private[this] final val ID_NAME = QuarryPlus.modID + ":blacklist_name"
  private[this] final val ID_MOD = QuarryPlus.modID + ":blacklist_modid"
  private[this] final val ID_ORES = QuarryPlus.modID + ":blacklist_ores"

  object Entry extends AnyRef with JsonSerializer[Entry] with JsonDeserializer[Entry] {
    override def serialize(src: Entry, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
      val o = new JsonObject
      o.addProperty("id", src.id.toString)
      src match {
        case Mod(modID) => o.addProperty("modID", modID)
        case Name(name) => o.addProperty("name", name.toString)
        case _ =>
      }
      LOGGER.debug(s"BlackList, $src, was serialized to $o.")
      o
    }

    override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Entry = {
      val idOpt = for {
        j <- Option(json) if j.isJsonObject
        obj = j.getAsJsonObject
        id <- Option(obj.get("id")).map(_.getAsString) if obj.has("id")
      } yield {
        id match {
          case ID_NAME => Name(new ResourceLocation(JSONUtils.getString(obj, "name")))
          case ID_MOD => Mod(JSONUtils.getString(obj, "modID"))
          case ID_ORES => Ores
          case _ => Air
        }
      }
      LOGGER.debug("BlackList, {}, created from json, {}", idOpt.orNull, json)
      idOpt.getOrElse(Air)
    }
  }

  private object EntryArray extends JsonDeserializer[Array[Entry]] {
    override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Array[Entry] = {
      val componentType = typeOfT.asInstanceOf[GenericArrayType].getGenericComponentType
      if (json.isJsonArray) CollectionConverters.asScala(json.getAsJsonArray).map(j => context.deserialize[Entry](j, componentType)).toArray
      else Array(context.deserialize[Entry](json, componentType))
    }
  }

  private object Air extends Entry(ID_AIR) {
    override def test(state: BlockState, world: World, pos: BlockPos): Boolean = world.isAirBlock(pos)

    override def toString = "BlackList of Air"
  }

  private case class Name(name: ResourceLocation) extends Entry(ID_NAME) {
    override def test(state: BlockState, world: World, pos: BlockPos): Boolean = state.getBlock.getRegistryName == name

    override def toString = "BlackList for " + name
  }

  private case class Mod(modID: String) extends Entry(ID_MOD) {
    override def test(state: BlockState, world: World, pos: BlockPos): Boolean = {
      val registryName = state.getBlock.getRegistryName
      if (registryName == null) false
      else registryName.getNamespace == modID
    }

    override def toString = "BlackList for all blocks of " + modID
  }

  private object Ores extends Entry(ID_ORES) {
    private[this] var cacheNoOre = Set(Blocks.AIR.getDefaultState, Blocks.CAVE_AIR.getDefaultState, Blocks.VOID_AIR.getDefaultState)
    private[this] var cacheOre = Set(
      Blocks.COAL_ORE.getDefaultState,
      Blocks.DIAMOND_ORE.getDefaultState,
      Blocks.EMERALD_ORE.getDefaultState,
      Blocks.GOLD_ORE.getDefaultState,
      Blocks.IRON_ORE.getDefaultState,
      Blocks.LAPIS_ORE.getDefaultState,
      Blocks.NETHER_QUARTZ_ORE.getDefaultState,
      Blocks.REDSTONE_ORE.getDefaultState
    )

    override def test(state: BlockState, world: World, pos: BlockPos): Boolean = {
      if (cacheNoOre(state)) false
      else if (cacheOre(state)) true
      else {
        if (Tags.Blocks.ORES.contains(state.getBlock)) {
          cacheOre += state
          true
        } else {
          cacheNoOre += state
          false
        }
      }
    }
  }

  val GSON = (new GsonBuilder).disableHtmlEscaping().setPrettyPrinting()
    .registerTypeHierarchyAdapter(classOf[Entry], Entry)
    .registerTypeHierarchyAdapter(classOf[Array[Entry]], EntryArray)
    .create()

  object Reload extends JsonReloadListener(QuarryBlackList.GSON, QuarryPlus.modID + "/blacklist") {
    override def apply(objectIn: util.Map[ResourceLocation, JsonElement], resourceManagerIn: IResourceManager, profilerIn: IProfiler): Unit = {
      val set = CollectionConverters.asScala(objectIn).map { case (_, element) => GSON.fromJson(element, classOf[Array[Entry]]) }
        .flatten.toSet
      QuarryBlackList.entries = set + Air // Air is unbreakable!
      LOGGER.debug("QuarryBlackList loaded {} entries.", QuarryBlackList.entries.size)
    }
  }

}
