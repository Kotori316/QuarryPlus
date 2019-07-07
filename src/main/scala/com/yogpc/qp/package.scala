package com.yogpc

import cats._
import cats.data._
import cats.implicits._
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.nbt._
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.{INBTSerializable, LazyOptional, NonNullSupplier}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.registries.ForgeRegistries

package object qp {

  type NBTWrapper[A, NBTType <: INBTBase] = A => NBTType
  type Cap[T] = OptionT[Eval, T]

  val enchantCollector: PartialFunction[(ResourceLocation, Int), (ResourceLocation, Integer)] = {
    case (a, b) if b > 0 => (a, Int.box(b))
  }
  val toComponentString: String => TextComponentString = s => new TextComponentString(s)
  val nonNull: AnyRef => Boolean = obj => obj != null
  val facings = Eval.later(EnumFacing.values()).map(_.toList)

  def toJavaOption[T](o: Option[T]): java.util.Optional[T] = {
    //I think it's faster than match function.
    if (o.isDefined) {
      java.util.Optional.ofNullable(o.get)
    } else {
      java.util.Optional.empty()
    }
  }

  def toScalaOption[T](o: java.util.Optional[T]): Option[T] = {
    if (o.isPresent) {
      Some(o.get())
    } else {
      None
    }
  }

  implicit class SOM[T](val o: java.util.Optional[T]) extends AnyVal {
    def scalaMap[B](f: T => B): Option[B] = toScalaOption(o).map(f)

    def scalaFilter(p: T => Boolean): Option[T] = toScalaOption(o).filter(p)

    def asScala: Option[T] = toScalaOption(o)
  }

  implicit class JOS[T](val o: Option[T]) extends AnyVal {
    def asJava: java.util.Optional[T] = toJavaOption(o)
  }

  implicit class ItemStackRemoveEnchantment(val stack: ItemStack) extends AnyVal {
    def removeEnchantment(enchantment: Enchantment): Unit = {
      val id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment)
      val tagName = if (stack.getItem == net.minecraft.init.Items.ENCHANTED_BOOK) "StoredEnchantments" else "ench"
      val list = Option(stack.getTag).fold(new NBTTagList)(_.getList(tagName, NBT.TAG_COMPOUND))

      import scala.collection.JavaConverters._
      val copied = list.asScala.zipWithIndex.map { case (t, i) => (t.asInstanceOf[NBTTagCompound], i) }
      for ((tag, i) <- copied) {
        if (tag.getString("id") == id.toString) {
          list.removeTag(i)
        }
      }
      Option(stack.getTag).foreach(subtag => {
        subtag.remove(tagName)
        if (!list.isEmpty) {
          subtag.put(tagName, list)
        }
        stack.setTag(if (subtag.isEmpty) null else subtag)
      })
    }

    def enchantmentAdded(enchantment: Enchantment, level: Int): ItemStack = {
      stack.addEnchantment(enchantment, level)
      stack
    }
  }

  implicit val Long2NBT: NBTWrapper[Long, NBTTagLong] = (num: Long) => new NBTTagLong(num)
  implicit val int2NBT: NBTWrapper[Int, NBTTagInt] = (num: Int) => new NBTTagInt(num)

  implicit val Fluid2NBT: NBTWrapper[FluidStack, NBTTagCompound] = (num: FluidStack) => num.writeToNBT(new NBTTagCompound)
  implicit val NBTSerializable2NBT: INBTSerializable[NBTTagCompound] NBTWrapper NBTTagCompound = _.serializeNBT()

  implicit class NumberToNbt[A](private val num: A) extends AnyVal {
    def toNBT[B <: INBTBase](implicit wrapper: NBTWrapper[A, B]): B = wrapper apply num
  }

  implicit class PosHelper(val blockPos: BlockPos) extends AnyVal {
    def offset(facing1: EnumFacing, facing2: EnumFacing): BlockPos = {
      val x = facing1.getXOffset + facing2.getXOffset
      val y = facing1.getYOffset + facing2.getYOffset
      val z = facing1.getZOffset + facing2.getZOffset
      blockPos.add(x, y, z)
    }

    def copy(x: Int = blockPos.getX, y: Int = blockPos.getY, z: Int = blockPos.getZ): BlockPos = {
      if (x == blockPos.getX && y == blockPos.getY && z == blockPos.getZ) {
        blockPos.toImmutable
      } else {
        new BlockPos(x, y, z)
      }
    }
  }

  def transform0[T](cap: LazyOptional[T]) = Eval.always {
    if (cap.isPresent) {
      cap.orElseThrow(thrower).some
    } else {
      None
    }
  }

  implicit class AsScalaLO[T](val cap: LazyOptional[T]) extends AnyVal {
    def asScala: Cap[T] = OptionT(transform0(cap))
  }

  private val thrower: NonNullSupplier[AssertionError] = () =>
    new AssertionError(
      "LazyOptional has no content " +
        "though it returned true when isPresent is called.")

  implicit val showPos: Show[BlockPos] = pos => s"(${pos.getX}, ${pos.getY}, ${pos.getZ})"
}
