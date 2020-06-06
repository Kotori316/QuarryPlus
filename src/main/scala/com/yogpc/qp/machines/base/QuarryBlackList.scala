package com.yogpc.qp.machines.base

import java.lang.reflect.{GenericArrayType, Type}
import java.util

import com.google.gson._
import com.mojang.brigadier.StringReader
import com.mojang.datafixers.Dynamic
import com.mojang.datafixers.types.{DynamicOps, JsonOps}
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.utils.JsonReloadListener
import com.yogpc.qp.{NBTWrapper, QuarryPlus}
import net.minecraft.block.{Block, BlockState, Blocks}
import net.minecraft.command.arguments.BlockPredicateArgument
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps}
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{CachedBlockInfo, ResourceLocation}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.common.Tags
import org.apache.logging.log4j.LogManager

import scala.jdk.javaapi.CollectionConverters
import scala.util.Try

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
  def contains(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = entries.exists(_.test(state, world, pos))

  final def example1: Seq[Entry] = Seq(Air)

  final def example2: Seq[Entry] = Seq(Air, Name(Blocks.WHITE_WOOL.getRegistryName), Mod("ic2"), Ores, Tag(Tags.Blocks.STONE.getId))

  private var entries: Set[Entry] = Set.empty

  abstract class Entry(val id: String) {
    def test(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean
  }

  private[this] final val ID_AIR = QuarryPlus.modID + ":blacklist_air"
  private[this] final val ID_NAME = QuarryPlus.modID + ":blacklist_name"
  private[this] final val ID_MOD = QuarryPlus.modID + ":blacklist_modid"
  private[this] final val ID_ORES = QuarryPlus.modID + ":blacklist_ores"
  private[this] final val ID_TAG = QuarryPlus.modID + ":blacklist_tag"
  private[this] final val ID_VANILLA = QuarryPlus.modID + ":blacklist_vanilla"
  private[this] final val ID_FLUID = QuarryPlus.modID + ":blacklist_fluid"

  def writeEntry[A](src: Entry, ops: DynamicOps[A]): A = {
    val map: Map[A, A] = Map(
      "id" -> ops.createString(src.id),
      src match {
        case Mod(modID) => "modID" -> ops.createString(modID)
        case Name(name) => "name" -> ops.createString(name.toString)
        case Tag(name) => "tag" -> ops.createString(name.toString)
        case VanillaBlockPredicate(block_predicate) => "block_predicate" -> ops.createString(block_predicate)
        case _ => "" -> ops.empty()
      }
    ).collect { case (str, a) if !str.isEmpty => ops.createString(str) -> a }
    val o = ops.createMap(CollectionConverters.asJava(map))
    LOGGER.debug(s"BlackListEntry, $src, was serialized to $o.")
    o
  }

  def readEntry[A](tagLike: Dynamic[A]): Entry = {
    import com.yogpc.qp._
    val idOpt = for {
      j <- tagLike.asMapOpt[String, Dynamic[A]](_.asString(""), java.util.function.Function.identity()).asScala
      map = CollectionConverters.asScala(j)
      id <- map.get("id").flatMap(_.asString().asScala)
    } yield {
      id match {
        case ID_NAME => Name(new ResourceLocation(map("name").asString.get()))
        case ID_MOD => Mod(map("modID").asString().get())
        case ID_TAG => Tag(new ResourceLocation(map("tag").asString().get()))
        case ID_VANILLA => VanillaBlockPredicate(map("block_predicate").asString().get())
        case ID_ORES => Ores
        case ID_FLUID => Fluids
        case _ => Air
      }
    }
    LOGGER.debug("BlackListEntry, {}, created from {}, {}", idOpt.orNull, tagLike.getValue.getClass.getSimpleName, tagLike.getValue)
    idOpt.getOrElse(Air)
  }

  object Entry extends AnyRef with JsonSerializer[Entry] with JsonDeserializer[Entry] {
    override def serialize(src: Entry, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
      writeEntry(src, JsonOps.INSTANCE)
    }

    override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Entry = {
      readEntry(new Dynamic(JsonOps.INSTANCE, json))
    }

    implicit val EntryToNBT: NBTWrapper[Entry, CompoundNBT] = writeEntry(_, NBTDynamicOps.INSTANCE).asInstanceOf[CompoundNBT]
  }

  private object EntryArray extends JsonDeserializer[Array[Entry]] {
    override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Array[Entry] = {
      val componentType = typeOfT.asInstanceOf[GenericArrayType].getGenericComponentType
      if (json.isJsonArray) CollectionConverters.asScala(json.getAsJsonArray).map(j => context.deserialize[Entry](j, componentType)).toArray
      else Array(context.deserialize[Entry](json, componentType))
    }
  }

  object Air extends Entry(ID_AIR) {
    override def test(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = world.getBlockState(pos).isAir(world, pos)

    override def toString = "BlackList of Air"
  }

  case class Name(name: ResourceLocation) extends Entry(ID_NAME) {
    override def test(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = state.getBlock.getRegistryName == name

    override def toString: String = name.toString
  }

  case class Mod(modID: String) extends Entry(ID_MOD) {
    override def test(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = {
      val registryName = state.getBlock.getRegistryName
      if (registryName == null) false
      else registryName.getNamespace == modID
    }

    override def toString: String = "BlackList for all blocks of " + modID
  }

  object Ores extends Entry(ID_ORES) {
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

    override def test(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = {
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

    override def toString = "BlackList of tag `forge:ores`."
  }

  case class Tag(name: ResourceLocation) extends Entry(ID_TAG) {

    import net.minecraft.tags.{BlockTags, Tag => MCTag}

    private[this] final val tag: MCTag[Block] = BlockTags.getCollection.get(name)

    override def test(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = {
      if (tag == null) false
      else tag.contains(state.getBlock)
    }

    override def toString: String = "BlackList for tag " + name
  }

  case class VanillaBlockPredicate(block_predicate: String) extends Entry(ID_VANILLA) {
    private lazy val iResult: Try[BlockPredicateArgument.IResult] =
      Try(BlockPredicateArgument.blockPredicate().parse(new StringReader(block_predicate)))

    override def test(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean = {
      world match {
        case w: World =>
          iResult.map(_.create(w.getTags).test(new CachedBlockInfo(w, pos, false))).recover { e =>
            LOGGER.debug(s"Predicate '$block_predicate' is invalid.", e)
            false
          }.getOrElse(false)
        case _ => false
      }
    }

    override def toString: String = s"Vanilla Predicate[$block_predicate]"
  }

  object Fluids extends Entry(ID_FLUID) {
    override def test(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean =
      TilePump.isLiquid(state)

    override def toString: String = "BlackList of fluids."
  }

  private final val GSON: Gson = (new GsonBuilder).disableHtmlEscaping().setPrettyPrinting()
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
