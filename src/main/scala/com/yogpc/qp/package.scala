package com.yogpc

import com.yogpc.qp.version.VersionUtil
import net.minecraft.item.ItemStack

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

    implicit class ISHelper(val stack: ItemStack) extends AnyVal {
        def getCount: Int = VersionUtil.getCount(stack)
    }

}
