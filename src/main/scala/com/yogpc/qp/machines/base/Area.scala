package com.yogpc.qp.machines.base

import cats.Show
import cats.data.{OptionT, ValidatedNel}
import cats.implicits._
import com.mojang.datafixers.Dynamic
import com.yogpc.qp._
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps}
import net.minecraft.util.Direction
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos, Vec3i}
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.server.ServerWorld

case class Area(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int, dimID: Option[Int]) {
  private val dimensionType = dimID.flatMap(i => Option(DimensionType.getById(i))).orNull

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
  val zeroArea = Area(0, 0, 0, 0, 0, 0, None)

  implicit val showArea: Show[Area] = area => s"(${area.xMin}, ${area.yMin}, ${area.zMin}) -> (${area.xMax}, ${area.yMax}, ${area.zMax}) in dim ${area.dimID.map(_.toString).getOrElse(EMPTY_DIM)}"

  implicit val areaToNbt: Area NBTWrapper CompoundNBT = area => {
    val nbt = new CompoundNBT
    nbt.putInt(NBT_X_MIN, area.xMin)
    nbt.putInt(NBT_X_MAX, area.xMax)
    nbt.putInt(NBT_Y_MIN, area.yMin)
    nbt.putInt(NBT_Y_MAX, area.yMax)
    nbt.putInt(NBT_Z_MIN, area.zMin)
    nbt.putInt(NBT_Z_MAX, area.zMax)
    area.dimID.flatMap(id => Option(DimensionType.getById(id))).foreach(t => nbt.put(NBT_DIM, t.serialize(NBTDynamicOps.INSTANCE)))
    nbt
  }

  val areaLengthSq: Area => Double = {
    case Area(xMin, _, zMin, xMax, yMax, zMax, _) =>
      Math.pow(xMax - xMin, 2) + Math.pow(yMax, 2) + Math.pow(zMax - zMin, 2)
  }
  val areaBox: Area => AxisAlignedBB = area =>
    new AxisAlignedBB(area.xMin, 0, area.zMin, area.xMax, area.yMax, area.zMax)

  def posToArea(p1: Vec3i, p2: Vec3i, dim: DimensionType): Area = {
    Area(Math.min(p1.getX, p2.getX), Math.min(p1.getY, p2.getY), Math.min(p1.getZ, p2.getZ),
      Math.max(p1.getX, p2.getX), Math.max(p1.getY, p2.getY), Math.max(p1.getZ, p2.getZ), dim.getId.some)
  }

  def defaultQuarryArea(pos: BlockPos, facing: Direction, dim: DimensionType): Area = {
    val x = 11
    val y = (x - 1) / 2 //5
    val start: BlockPos = pos.offset(facing, 2)
    val front: BlockPos = start.offset(facing.rotateY(), y)
    val edge1: BlockPos = front.up(3)
    val pos1: BlockPos = start.offset(facing, x - 1)
    val edge2: BlockPos = pos1.offset(facing.rotateYCCW(), y)
    posToArea(edge1, edge2, dim)
  }

  def defaultAdvQuarryArea(pos: BlockPos, dim: DimensionType): Area = {
    val chunkPos = new ChunkPos(pos)
    val y = pos.getY
    Area(chunkPos.getXStart, y, chunkPos.getZStart, chunkPos.getXEnd, y, chunkPos.getZEnd, dim.getId.some)
  }

  def getMarkersOnDirection(directions: List[Direction], world: World, pos: BlockPos): OptionT[List, IMarker] =
    for {
      f <- OptionT.liftF(directions)
      p <- OptionT.pure[List](pos.offset(f))
      t <- OptionT.fromOption[List](Option(world.getTileEntity(p)))
      marker <- t.getCapability(IMarker.Cap.MARKER_CAPABILITY(), f.getOpposite).asScala.mapK(evalToList) orElse
        OptionT.pure[List](t).collect { case marker: IMarker => marker }
      if marker.hasLink
    } yield marker

  def findQuarryArea(facing: Direction, world: World, pos: BlockPos) = {
    val mayMarker = getMarkersOnDirection(List(facing.getOpposite, facing.rotateY(), facing.rotateYCCW()), world, pos)
    mayMarker.collectFirst(PartialFunction.fromFunction(identity)) match {
      case Some(marker) => areaFromMarker(facing, pos, marker, world.getDimension.getType)
      case None => defaultQuarryArea(pos, facing.getOpposite, world.getDimension.getType) -> None
    }
  }

  def areaFromMarker(facing: Direction, pos: BlockPos, marker: IMarker, dim: DimensionType) = {
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

  def areaLoad(nbt: CompoundNBT) = {
    val dim = nbt.some.filter(_.contains(NBT_DIM)).map(t => DimensionType.func_218271_a(new Dynamic(NBTDynamicOps.INSTANCE, t.get(NBT_DIM))))
    Area(nbt.getInt(NBT_X_MIN), nbt.getInt(NBT_Y_MIN), nbt.getInt(NBT_Z_MIN), nbt.getInt(NBT_X_MAX), nbt.getInt(NBT_Y_MAX), nbt.getInt(NBT_Z_MAX), dim.map(_.getId))
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
      case (_, None) => defaultAdvQuarryArea(pos, world.getDimension.getType) -> None
    }
  }

  def limit(area: Area, limit: Int): ValidatedNel[String, Area] = {
    if (limit == -1) return area.validNel // Fast return to skip limitation check

    def xCheck(area: Area): ValidatedNel[String, Area] = if (area.xMax - area.xMin > limit) s"Over limit x. Limit $limit but ${area.xMin} -> ${area.xMax}".invalidNel else area.validNel

    def zCheck(area: Area): ValidatedNel[String, Area] = if (area.zMax - area.zMin > limit) s"Over limit z. Limit $limit but ${area.zMin} -> ${area.zMax}".invalidNel else area.validNel

    (xCheck(area), zCheck(area)).mapN { case (_, _) => area }
  }
}
