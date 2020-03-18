package com.yogpc.qp.tile

import java.lang.reflect.Type
import java.nio.file.{Files, Path, Paths}
import java.util.Collections

import com.google.gson._
import com.yogpc.qp.QuarryPlus
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{JsonUtils, ResourceLocation}
import net.minecraft.world.World

object QuarryBlackList {
  //noinspection SpellCheckingInspection
  private[this] final lazy val entries: Set[Entry] = fromJson(Paths.get("config", QuarryPlus.modID, "blacklist.json"))

  def fromJson(path: Path): Set[Entry] = {
    if (Files.exists(path)) {
      val GSON = (new GsonBuilder).registerTypeHierarchyAdapter(classOf[Entry], Entry).create()
      val r = GSON.fromJson(Files.newBufferedReader(path), classOf[Array[Entry]])
      r.toSet
    } else {
      val GSON = (new GsonBuilder).registerTypeHierarchyAdapter(classOf[Entry], Entry).setPrettyPrinting().create()
      val s = Array(Air)
      Files.write(path, Collections.singletonList(GSON.toJson(s)))
      s.toSet
    }
  }

  def contains(state: IBlockState, world: World, pos: BlockPos): Boolean = {
    entries.exists(_.test(state, world, pos))
  }

  abstract class Entry(val id: ResourceLocation) {
    def this(id: String) = {
      this(new ResourceLocation(QuarryPlus.modID, id))
    }

    def test(state: IBlockState, world: World, pos: BlockPos): Boolean
  }

  object Entry extends AnyRef with JsonSerializer[Entry] with JsonDeserializer[Entry] {
    override def serialize(src: Entry, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
      val o = new JsonObject
      o.addProperty("id", src.id.toString)
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
      } yield id
      idOpt match {
        case Some(x) =>
          x match {
            case "quarryplus:blacklist_name" => Name(new ResourceLocation(JsonUtils.getString(json, "name")))
            case "quarryplus:blacklist_modid" => Mod(JsonUtils.getString(json, "modID"))
            case _ => Air
          }
        case None => Air
      }
    }
  }

  private object Air extends Entry("blacklist_air") {
    override def test(state: IBlockState, world: World, pos: BlockPos): Boolean = world.isAirBlock(pos)
  }

  private case class Name(name: ResourceLocation) extends Entry("blacklist_name") {
    override def test(state: IBlockState, world: World, pos: BlockPos): Boolean = state.getBlock.getRegistryName == name
  }

  private case class Mod(modID: String) extends Entry("blacklist_modid") {
    override def test(state: IBlockState, world: World, pos: BlockPos): Boolean = {
      val registryName = state.getBlock.getRegistryName
      if (registryName == null) false
      else registryName.getResourceDomain == modID
    }
  }

}
