package com.yogpc.qp.tile

import java.lang.reflect.Type
import java.nio.file.{Files, Path, Paths}
import java.util.Collections

import com.google.gson._
import com.yogpc.qp.QuarryPlus
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{JsonUtils, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.oredict.OreDictionary

import scala.util.Try

object QuarryBlackList {
  private[this] final lazy val entries: Set[Entry] = fromJson(Paths.get("config", QuarryPlus.modID, "blacklist.json"))

  def fromJson(path: Path): Set[Entry] = Try {
    if (Files.exists(path)) {
      val GSON = (new GsonBuilder).registerTypeHierarchyAdapter(classOf[Entry], Entry).create()
      val r = GSON.fromJson(Files.newBufferedReader(path), classOf[Array[Entry]])
      r.toSet + Air
    } else {
      val GSON = (new GsonBuilder).registerTypeHierarchyAdapter(classOf[Entry], Entry).setPrettyPrinting().create()
      val s: Array[Entry] = Array(Air)
      Files.createDirectories(path.getParent)
      Files.write(path, Collections.singletonList(GSON.toJson(s)))
      s.toSet + Air
    }
  }.recover {
    case e: Exception =>
      QuarryPlus.LOGGER.warn("Caught error in loading black list.", e)
      Set(Air: Entry)
  }.getOrElse(Set(Air: Entry))

  def contains(state: IBlockState, world: World, pos: BlockPos): Boolean = {
    entries.exists(_.test(state, world, pos))
  }

  abstract class Entry(val id: String) {
    def test(state: IBlockState, world: World, pos: BlockPos): Boolean
  }

  private[this] final val ID_AIR = QuarryPlus.modID + ":blacklist_air"
  private[this] final val ID_NAME = QuarryPlus.modID + ":blacklist_name"
  private[this] final val ID_MOD = QuarryPlus.modID + ":blacklist_modid"
  private[this] final val ID_ORES = QuarryPlus.modID + ":blacklist_ores"

  object Entry extends AnyRef with JsonSerializer[Entry] with JsonDeserializer[Entry] {
    override def serialize(src: Entry, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
      val o = new JsonObject
      o.addProperty("id", src.id)
      src match {
        case Mod(modID) => o.addProperty("modID", modID)
        case Name(name) => o.addProperty("name", name.toString)
        case _ =>
      }
      o
    }

    override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Entry = {
      val idOpt = for {
        j <- Option(json) if j.isJsonObject
        obj = j.getAsJsonObject
        id <- Option(obj.get("id")).map(_.getAsString) if obj.has("id")
      } yield {
        id match {
          case ID_NAME => Name(new ResourceLocation(JsonUtils.getString(obj, "name")))
          case ID_MOD => Mod(JsonUtils.getString(obj, "modID"))
          case ID_ORES => Ores
          case _ => Air
        }
      }
      idOpt.getOrElse(Air)
    }
  }

  private object Air extends Entry(ID_AIR) {
    override def test(state: IBlockState, world: World, pos: BlockPos): Boolean = world.isAirBlock(pos)

    override def toString = "BlackList of Air"
  }

  private case class Name(name: ResourceLocation) extends Entry(ID_NAME) {
    override def test(state: IBlockState, world: World, pos: BlockPos): Boolean = state.getBlock.getRegistryName == name

    override def toString = "BlackList for " + name
  }

  private case class Mod(modID: String) extends Entry(ID_MOD) {
    override def test(state: IBlockState, world: World, pos: BlockPos): Boolean = {
      val registryName = state.getBlock.getRegistryName
      if (registryName == null) false
      else registryName.getResourceDomain == modID
    }

    override def toString = "BlackList for all blocks of " + modID
  }

  private object Ores extends Entry(ID_ORES) {
    private[this] var cacheNoOre = Set(Blocks.AIR.getDefaultState)
    private[this] var cacheOre = Set(
      Blocks.COAL_ORE.getDefaultState,
      Blocks.DIAMOND_ORE.getDefaultState,
      Blocks.EMERALD_ORE.getDefaultState,
      Blocks.GOLD_ORE.getDefaultState,
      Blocks.IRON_ORE.getDefaultState,
      Blocks.LAPIS_ORE.getDefaultState,
      Blocks.LIT_REDSTONE_ORE.getDefaultState,
      Blocks.QUARTZ_ORE.getDefaultState,
      Blocks.REDSTONE_ORE.getDefaultState
    )

    override def test(state: IBlockState, world: World, pos: BlockPos): Boolean = {
      if (cacheNoOre(state)) false
      else if (cacheOre(state)) true
      else {
        val meta = state.getBlock.getMetaFromState(state)
        val stack = new ItemStack(state.getBlock, 1, meta)
        if (stack.isEmpty) {
          cacheNoOre += state
          false
        } else {
          val oreDictionaryName = OreDictionary.getOreIDs(stack)
            .map(OreDictionary.getOreName)
          if (oreDictionaryName.exists(_.startsWith("ore"))) {
            cacheOre += state
            true
          } else {
            cacheNoOre += state
            false
          }
        }
      }
    }
  }

}
