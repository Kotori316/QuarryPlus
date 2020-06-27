package com.yogpc.qp.machines.base

import cats.Show
import cats.data.{OptionT, ValidatedNel}
import cats.implicits._
import com.yogpc.qp._
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps}
import net.minecraft.util.math.vector.Vector3i
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos}
import net.minecraft.util.registry.Registry
import net.minecraft.util.{Direction, RegistryKey, ResourceLocation}
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld

case class Area(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int, dimID: Option[ResourceLocation]) {
  private val dimensionType = dimID.flatMap(i => Option(Area.idToType(i))).orNull

  def getWorld(world: ServerWorld): ServerWorld = {
    if (dimensionType != null)
      world.getServer.getWorld(dimensionType)
    else
      world
  }
}

object Area {
  private[this] final val NBT_X_MIN = "xMin"
  private[this] final val NBT_X_MAX = "xMax"
  private[this] final val NBT_Y_MIN = "yMin"
  private[this] final val NBT_Y_MAX = "yMax"
  private[this] final val NBT_Z_MIN = "zMin"
  private[this] final val NBT_Z_MAX = "zMax"
  private[this] final val NBT_DIM = "dim"
  private[this] final val EMPTY_DIM = "None"
  val zeroArea: Area = Area(0, 0, 0, 0, 0, 0, None)

  private def idToType(location: ResourceLocation): RegistryKey[World] = RegistryKey.func_240903_a_(Registry.field_239699_ae_, location)

  implicit val showArea: Show[Area] = area => s"(${area.xMin}, ${area.yMin}, ${area.zMin}) -> (${area.xMax}, ${area.yMax}, ${area.zMax}) in dim ${area.dimID.map(_.toString).getOrElse(EMPTY_DIM)}"

  implicit val areaToNbt: Area NBTWrapper CompoundNBT = area => {
    val nbt = new CompoundNBT
    nbt.putInt(NBT_X_MIN, area.xMin)
    nbt.putInt(NBT_X_MAX, area.xMax)
    nbt.putInt(NBT_Y_MIN, area.yMin)
    nbt.putInt(NBT_Y_MAX, area.yMax)
    nbt.putInt(NBT_Z_MIN, area.zMin)
    nbt.putInt(NBT_Z_MAX, area.zMax)

    area.dimID.flatMap(id => Option(idToType(id)))
    (area.dimID
      >>= (id => Option(idToType(id)))
      >>= (t => World.field_234917_f_.encodeStart(NBTDynamicOps.INSTANCE, t).result().asScala))
      .foreach(t => nbt.put(NBT_DIM, t))
    nbt
  }

  val areaLengthSq: Area => Double = {
    case Area(xMin, _, zMin, xMax, yMax, zMax, _) =>
      Math.pow(xMax - xMin, 2) + Math.pow(yMax, 2) + Math.pow(zMax - zMin, 2)
  }
  val areaBox: Area => AxisAlignedBB = area =>
    new AxisAlignedBB(area.xMin, 0, area.zMin, area.xMax, area.yMax, area.zMax)

  def posToArea(p1: Vector3i, p2: Vector3i, dim: RegistryKey[World]): Area = posToArea(p1, p2, dim.func_240901_a_().some)

  def posToArea(p1: Vector3i, p2: Vector3i, dim: Option[ResourceLocation]): Area = {
    Area(Math.min(p1.getX, p2.getX), Math.min(p1.getY, p2.getY), Math.min(p1.getZ, p2.getZ),
      Math.max(p1.getX, p2.getX), Math.max(p1.getY, p2.getY), Math.max(p1.getZ, p2.getZ), dim)
  }

  def defaultQuarryArea(pos: BlockPos, facing: Direction, dim: RegistryKey[World]): Area = defaultQuarryArea(pos, facing, dim.func_240901_a_().some)

  def defaultQuarryArea(pos: BlockPos, facing: Direction, dim: Option[ResourceLocation]): Area = {
    val x = 11
    val y = (x - 1) / 2 //5
    val start: BlockPos = pos.offset(facing, 2)
    val front: BlockPos = start.offset(facing.rotateY(), y)
    val edge1: BlockPos = front.up(3)
    val pos1: BlockPos = start.offset(facing, x - 1)
    val edge2: BlockPos = pos1.offset(facing.rotateYCCW(), y)
    posToArea(edge1, edge2, dim)
  }

  def defaultAdvQuarryArea(pos: BlockPos, dim: RegistryKey[World]): Area = {
    val chunkPos = new ChunkPos(pos)
    val y = pos.getY
    Area(chunkPos.getXStart, y, chunkPos.getZStart, chunkPos.getXEnd, y, chunkPos.getZEnd, dim.func_240901_a_().some)
  }

  def getMarkersOnDirection(directions: List[Direction], world: World, pos: BlockPos): OptionT[List, IMarker] =
    getMarkersOnDirection(directions, world, pos, ignoreHasLink = false)

  def getMarkersOnDirection(directions: List[Direction], world: World, pos: BlockPos, ignoreHasLink: Boolean): OptionT[List, IMarker] =
    for {
      f <- OptionT.liftF(directions)
      p = pos.offset(f)
      t <- OptionT.fromOption[List](Option(world.getTileEntity(p)))
      markerCap = t.getCapability(IMarker.Cap.MARKER_CAPABILITY(), f.getOpposite).asScala.mapK(evalToList)
      marker <- markerCap orElse OptionT.pure[List](t).collect { case marker: IMarker => marker }
      if ignoreHasLink || marker.hasLink
    } yield marker

  def findQuarryArea(facing: Direction, world: World, pos: BlockPos): (Area, Option[IMarker]) = {
    val mayMarker = getMarkersOnDirection(List(facing.getOpposite, facing.rotateY(), facing.rotateYCCW()), world, pos)
    mayMarker.map(marker => areaFromMarker(facing, pos, marker, world.func_234923_W_()))
      .filter { case (_, maybeMarker) => maybeMarker.isDefined }
      .collectFirst(PartialFunction.fromFunction(identity)) match {
      case Some(a) => a
      case None => defaultQuarryArea(pos, facing.getOpposite, world.func_234923_W_()) -> None
    }
  }

  def areaFromMarker(facing: Direction, pos: BlockPos, marker: IMarker, dim: RegistryKey[World]): (Area, Option[IMarker]) = areaFromMarker(facing, pos, marker, dim.func_240901_a_().some)

  def areaFromMarker(facing: Direction, pos: BlockPos, marker: IMarker, dim: Option[ResourceLocation]): (Area, Option[IMarker]) = {
    if (marker.min().getX <= pos.getX && marker.max().getX >= pos.getX &&
      marker.min().getY <= pos.getY && marker.max().getY >= pos.getY &&
      marker.min().getZ <= pos.getZ && marker.max().getZ >= pos.getZ) {
      defaultQuarryArea(pos, facing.getOpposite, dim) -> None
    } else {
      val subs = marker.max().subtract(marker.min())
      if (subs.getX > 1 && subs.getZ > 1) {
        val maxY = if (subs.getY > 1) marker.max().getY else marker.min().getY + 3
        posToArea(marker.min(), marker.max().copy(y = maxY), dim) -> Some(marker)
      } else {
        defaultQuarryArea(pos, facing.getOpposite, dim) -> None
      }
    }
  }

  def areaLoad(nbt: CompoundNBT): Area = {
    val dim = nbt.some.filter(_.contains(NBT_DIM)).flatMap(t => World.field_234917_f_.decode(NBTDynamicOps.INSTANCE, t.get(NBT_DIM)).result().asScala)
    Area(nbt.getInt(NBT_X_MIN), nbt.getInt(NBT_Y_MIN), nbt.getInt(NBT_Z_MIN), nbt.getInt(NBT_X_MAX), nbt.getInt(NBT_Y_MAX), nbt.getInt(NBT_Z_MAX), dim.map(_.getFirst.func_240901_a_()))
  }

  def posesInArea(area: Area, filter: (Int, Int, Int) => Boolean): List[BlockPos] = {
    val poses = for {
      x <- Range.inclusive(area.xMin, area.xMax)
      z <- Range.inclusive(area.zMin, area.zMax)
      y <- Range.inclusive(area.yMin, area.yMax).reverse
      if filter(x, y, z)
    } yield new BlockPos(x, y, z)
    poses.toList
  }

  def getFramePoses(area: Area): List[BlockPos] = {
    val builder = List.newBuilder[BlockPos]
    val minX = area.xMin
    val maxX = area.xMax
    val maxY = area.yMax
    val minZ = area.zMin
    val maxZ = area.zMax
    var i = 0
    while (i <= 4) {
      builder += new BlockPos(minX - 1, maxY + 4 - i, minZ - 1)
      builder += new BlockPos(minX - 1, maxY + 4 - i, maxZ + 1)
      builder += new BlockPos(maxX + 1, maxY + 4 - i, maxZ + 1)
      builder += new BlockPos(maxX + 1, maxY + 4 - i, minZ - 1)
      i += 1
    }
    var x = minX
    while (x <= maxX) {
      builder += new BlockPos(x, maxY + 4, minZ - 1)
      builder += new BlockPos(x, maxY + 0, minZ - 1)
      builder += new BlockPos(x, maxY + 0, maxZ + 1)
      builder += new BlockPos(x, maxY + 4, maxZ + 1)
      x += 1
    }
    var z = minZ
    while (z <= maxZ) {
      builder += new BlockPos(minX - 1, maxY + 4, z)
      builder += new BlockPos(minX - 1, maxY + 0, z)
      builder += new BlockPos(maxX + 1, maxY + 0, z)
      builder += new BlockPos(maxX + 1, maxY + 4, z)
      z += 1
    }
    builder.result()
  }

  def findAdvQuarryArea(facing: Direction, world: World, pos: BlockPos): (Area, Option[IMarker]) = {
    findQuarryArea(facing, world, pos) match {
      case marked@(_, Some(_)) => marked
      case (_, None) => defaultAdvQuarryArea(pos, world.func_234923_W_()) -> None
    }
  }

  def limit(area: Area, limit: Int): ValidatedNel[String, Area] = {
    if (limit == -1) return area.validNel // Fast return to skip limitation check

    def xCheck(area: Area): ValidatedNel[String, Area] = if (area.xMax - area.xMin > limit) s"Over limit x. Limit $limit but ${area.xMin} -> ${area.xMax}".invalidNel else area.validNel

    def zCheck(area: Area): ValidatedNel[String, Area] = if (area.zMax - area.zMin > limit) s"Over limit z. Limit $limit but ${area.zMin} -> ${area.zMax}".invalidNel else area.validNel

    (xCheck(area), zCheck(area)).mapN { case (_, _) => area }
  }
}
