package com.yogpc

import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.common.util.Constants.NBT

import scala.collection.AbstractIterator

package object qp {

    import scala.language.implicitConversions

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
        def tagIterator: Iterator[NBTTagCompound] = new NBTListIterator(list)
    }

    private class NBTListIterator(val list: NBTTagList) extends AbstractIterator[NBTTagCompound] {
        var count = 0

        override def hasNext: Boolean = count < list.tagCount()

        override def next(): NBTTagCompound = {
            val v = list.getCompoundTagAt(count)
            count += 1
            v
        }

        override def size: Int = list.tagCount()

        override def isEmpty: Boolean = list.hasNoTags
    }

    implicit class ItemStackRemoveEnch(val stack: ItemStack) {
        def removeEnchantment(ench: Enchantment): Unit = {
            val enchId = Enchantment.getEnchantmentID(ench).toShort
            val tagName = if (stack.getItem == net.minecraft.init.Items.ENCHANTED_BOOK) "StoredEnchantments" else "ench"
            val list = Option(stack.getTagCompound).map(_.getTagList(tagName, NBT.TAG_COMPOUND)).getOrElse(new NBTTagList)

            val copied = list.copy()
            for (i <- 0 until list.tagCount()) {
                val tag = copied.getCompoundTagAt(i)
                if (tag.getShort("id") == enchId) {
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

}
