package com.yogpc

import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTBase, NBTTagByte, NBTTagCompound, NBTTagInt, NBTTagList, NBTTagLong}
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack

import scala.collection.AbstractIterator

package object qp {

  val enchantCollector: PartialFunction[(Int, Int), (Integer, Integer)] = {
    case (a, b) if b > 0 => (Int.box(a), Int.box(b))
  }
  val toComponentString: String => TextComponentString = s => new TextComponentString(s)
  val nonNull: AnyRef => Boolean = obj => obj != null

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

  implicit class NBTList2Iterator(val list: NBTTagList) extends AnyVal {
    def tagIterator: Iterator[NBTTagCompound] = new AbstractIterator[NBTTagCompound] {
      var count = 0

      override def hasNext: Boolean = count < list.tagCount()

      override def next(): NBTTagCompound = {
        val v = list.getCompoundTagAt(count)
        count += 1
        v
      }

      override def size: Int = list.tagCount()

      override def hasDefiniteSize: Boolean = true
    }
  }

  implicit class ItemStackRemoveEnchantment(val stack: ItemStack) extends AnyVal {
    def removeEnchantment(enchantment: Enchantment): Unit = {
      val id = Enchantment.getEnchantmentID(enchantment).toShort
      val tagName = if (stack.getItem == net.minecraft.init.Items.ENCHANTED_BOOK) "StoredEnchantments" else "ench"
      val list = Option(stack.getTagCompound).fold(new NBTTagList)(_.getTagList(tagName, NBT.TAG_COMPOUND))

      val copied = list.copy()
      for (i <- 0 until list.tagCount()) {
        val tag = copied.getCompoundTagAt(i)
        if (tag.getShort("id") == id) {
          list.removeTag(i)
        }
      }
      Option(stack.getTagCompound).foreach(subtag => {
        subtag.removeTag(tagName)
        if (!list.hasNoTags) {
          subtag.setTag(tagName, list)
        }
        stack.setTagCompound(if (subtag.hasNoTags) null else subtag)
      })
    }
  }

  /**
    * Copied from [[https://yuroyoro.hatenablog.com/entry/20110323/1300854858]].
    *
    * @tparam A AnyRef
    */
  // 入れ物はタッパーです。
  implicit class Tapper[A](private val obj: A) extends AnyVal {
    // RubyのObject#tap的な。引数fに自分自身を適用させて自身を返す。
    // 副作用専用メソッド。nullだったらなにもしなーい
    def tap(f: A => Unit): A = {
      if (obj != null)
        f(obj)
      obj
    }

    // 上記の、戻り値Option版。nullだったらNoneが返る
    def tapOption(f: A => Unit): Option[A] = {
      Option(tap(f))
    }

    // いつでもmapできたら便利よね?
    def map[B](f: A => B): Option[B] = Option(obj).map(f)

    def nnMap[B](f: A => B): Option[B] = Option(obj).flatMap(f.andThen(Option.apply))

    // Option(obj)でもいいけど、何でもメソッドチェーンしたい病の人に
    def toOption: Option[A] = Option(obj)
  }

  type NBTWrapper[A, NBTType <: NBTBase] = A => NBTType
  implicit val Long2NBT: NBTWrapper[Long, NBTTagLong] = (num: Long) => new NBTTagLong(num)
  implicit val int2NBT: NBTWrapper[Int, NBTTagInt] = (num: Int) => new NBTTagInt(num)
  implicit val bool2NBT: NBTWrapper[Boolean, NBTTagByte] = (b: Boolean) => new NBTTagByte(if (b) 1 else 0)

  implicit val Fluid2NBT: NBTWrapper[FluidStack, NBTTagCompound] = (num: FluidStack) => num.writeToNBT(new NBTTagCompound)
  implicit val NBTSerializable2NBT: INBTSerializable[NBTTagCompound] NBTWrapper NBTTagCompound = _.serializeNBT()

  implicit class NumberToNbt[A](private val num: A) extends AnyVal {
    def toNBT[B <: NBTBase](implicit wrapper: NBTWrapper[A, B]): B = wrapper apply num
  }

  implicit class PosHelper(val blockPos: BlockPos) extends AnyVal {
    def offset(facing1: EnumFacing, facing2: EnumFacing): BlockPos = {
      val x = facing1.getFrontOffsetX + facing2.getFrontOffsetX
      val y = facing1.getFrontOffsetY + facing2.getFrontOffsetY
      val z = facing1.getFrontOffsetZ + facing2.getFrontOffsetZ
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

}
