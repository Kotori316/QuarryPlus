package com.yogpc.qp.machines.base

import cats.Show
import com.yogpc.qp._
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos, Vec3i}
import net.minecraft.world.World

case class Area(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int)

object Area {
  private[this] final val NBT_X_MIN = "xMin"
  private[this] final val NBT_X_MAX = "xMax"
  private[this] final val NBT_Y_MIN = "yMin"
  private[this] final val NBT_Y_MAX = "yMax"
  private[this] final val NBT_Z_MIN = "zMin"
  private[this] final val NBT_Z_MAX = "zMax"
  val zeroArea = Area(0, 0, 0, 0, 0, 0)

  implicit val showArea: Show[Area] = area => s"(${area.xMin}, ${area.yMin}, ${area.zMin}) -> (${area.xMax}, ${area.yMax}, ${area.zMax})"

  implicit val areaToNbt: Area NBTWrapper CompoundNBT = area => {
    val nbt = new CompoundNBT
    nbt.putInt(NBT_X_MIN, area.xMin)
    nbt.putInt(NBT_X_MAX, area.xMax)
    nbt.putInt(NBT_Y_MIN, area.yMin)
    nbt.putInt(NBT_Y_MAX, area.yMax)
    nbt.putInt(NBT_Z_MIN, area.zMin)
    nbt.putInt(NBT_Z_MAX, area.zMax)
    nbt
  }

  val areaLengthSq: Area => Double = {
    case Area(xMin, _, zMin, xMax, yMax, zMax) =>
      Math.pow(xMax - xMin, 2) + Math.pow(yMax, 2) + Math.pow(zMax - zMin, 2)
  }
  val areaBox: Area => AxisAlignedBB = area =>
    new AxisAlignedBB(area.xMin, 0, area.zMin, area.xMax, area.yMax, area.zMax)

  val posToArea: (Vec3i, Vec3i) => Area = {
    case (p1, p2) => Area(Math.min(p1.getX, p2.getX), Math.min(p1.getY, p2.getY), Math.min(p1.getZ, p2.getZ),
      Math.max(p1.getX, p2.getX), Math.max(p1.getY, p2.getY), Math.max(p1.getZ, p2.getZ))
  }

  def defaultQuarryArea(pos: BlockPos, facing: Direction): Area = {
    val x = 11
    val y = (x - 1) / 2 //5
    val start = pos.offset(facing, 2)
    val edge1 = start.offset(facing.rotateY(), y).up(3)
    val edge2 = start.offset(facing, x - 1).offset(facing.rotateYCCW(), y)
    posToArea(edge1, edge2)
  }

  def findQuarryArea(facing: Direction, world: World, pos: BlockPos) = {
    List(pos.offset(facing.getOpposite), pos.offset(facing.rotateY()), pos.offset(facing.rotateYCCW())).map(world.getTileEntity).collectFirst { case m: IMarker if m.hasLink => m } match {
      case Some(marker) => areaFromMarker(facing, pos, marker)
      case None => defaultQuarryArea(pos, facing.getOpposite) -> None
    }
  }

  def areaFromMarker(facing: Direction, pos: BlockPos, marker: IMarker) = {
    if (marker.min().getX <= pos.getX && marker.max().getX >= pos.getX &&
      marker.min().getY <= pos.getY && marker.max().getY >= pos.getY &&
      marker.min().getZ <= pos.getZ && marker.max().getZ >= pos.getZ) {
      defaultQuarryArea(pos, facing.getOpposite) -> None
    } else {
      val subs = marker.max().subtract(marker.min())
      if (subs.getX > 1 && subs.getZ > 1) {
        val maxY = if (subs.getY > 1) marker.max().getY else marker.min().getY + 3
        posToArea(marker.min(), marker.max().copy(y = maxY)) -> Some(marker)
      } else {
        defaultQuarryArea(pos, facing.getOpposite) -> None
      }
    }
  }

  def areaLoad(nbt: CompoundNBT) = {
    Area(nbt.getInt(NBT_X_MIN), nbt.getInt(NBT_Y_MIN), nbt.getInt(NBT_Z_MIN), nbt.getInt(NBT_X_MAX), nbt.getInt(NBT_Y_MAX), nbt.getInt(NBT_Z_MAX))
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
      case (_, None) =>
        val chunkPos = new ChunkPos(pos)
        val y = pos.getY
        Area(chunkPos.getXStart, y, chunkPos.getZStart, chunkPos.getXEnd, y, chunkPos.getZEnd) -> None
    }
  }
}
