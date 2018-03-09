package com.yogpc

import net.minecraft.nbt.{NBTTagCompound, NBTTagList}

import scala.collection.AbstractIterator

package object qp {

    import scala.language.implicitConversions

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

}
