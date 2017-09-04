package com.yogpc

package object qp {

    import scala.language.implicitConversions

    implicit def toJavaOption[T](o: Option[T]): java.util.Optional[T] = {
        //I think it's faster than match function.
        if (o.isDefined) {
            java.util.Optional.ofNullable(o.get)
        } else {
            java.util.Optional.empty()
        }
    }

    implicit def toScalaOption[T](o: java.util.Optional[T]): Option[T] = {
        if (o.isPresent) {
            Some(o.get())
        } else {
            None
        }
    }

    implicit class SOM[T](val o: java.util.Optional[T]) extends AnyVal {
        def scalaMap[B](f: T => B): Option[B] = toScalaOption(o).map(f)

        def scalaFilter(p: T => Boolean): Option[T] = toScalaOption(o).filter(p)
    }

}
