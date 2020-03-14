package com.yogpc.qp.utils

import java.util

import scala.collection.AbstractIterator

/**
  * Immutable map that holds the order of inserted objects.
  *
  * @tparam A key type
  * @tparam B value type
  */
class ListLikeMap[A, B] private(private val list: util.ArrayList[(A, B)]) {
  def isEmpty: Boolean = size == 0

  def nonEmpty: Boolean = !isEmpty

  def indexAt(key: A): Int = {
    var i = 0
    while (i < list.size()) {
      val e = list.get(i)
      if (e._1 == key)
        return i
      i += 1
    }
    /*return*/ -1
  }

  def get(key: A): Option[B] = {
    val index = indexAt(key)
    if (index == -1)
      None
    else
      Option(list.get(index)._2)
  }

  def getOrElse[B1 >: B](key: A, default: => B1): B1 = get(key).getOrElse(default)

  def updated[B1 >: B](key: A, value: B1, reuseInternalObjects: Boolean = false): ListLikeMap[A, B1] = {
    val index = indexAt(key)
    val copiedList = (if (reuseInternalObjects) this.list else list.clone()).asInstanceOf[util.ArrayList[(A, B1)]]
    if (index == -1) {
      // Just add to last.
      copiedList.add(key -> value)
    } else {
      copiedList.set(index, key -> value)
    }
    new ListLikeMap[A, B1](copiedList)
  }

  def +[B1 >: B](key: A, value: B1): ListLikeMap[A, B1] = updated(key, value)

  def remove(key: A, reuseInternalObjects: Boolean = false): ListLikeMap[A, B] = {
    val index = indexAt(key)
    if (index == -1) {
      this
    } else {
      if (this.size == 1) { // 1 - 1 = 0
        ListLikeMap.empty
      } else {
        val copiedList = (if (reuseInternalObjects) this.list else list.clone()).asInstanceOf[util.ArrayList[(A, B)]]
        copiedList.remove(index)
        new ListLikeMap[A, B](copiedList)
      }
    }
  }

  def -(key: A): ListLikeMap[A, B] = remove(key)

  def size: Int = list.size()

  def headOption: Option[(A, B)] = {
    if (list.isEmpty) None
    else Option(list.get(0))
  }

  def foreach[U](f: (A, B) => U): Unit = {
    var i = 0
    while (i < list.size()) {
      val (a, b) = list.get(i)
      f(a, b)
      i += 1
    }
  }

  def foldLeft[C](z: C)(op: (C, (A, B)) => C): C = {
    var i = 0
    var obj = z
    while (i < list.size()) {
      obj = op(obj, list.get(i))
      i += 1
    }
    obj
  }

  def map[C](f: (A, B) => C): List[C] = foldLeft(List.empty[C]) { case (l, t) => f(t._1, t._2) :: l }.reverse

  def valuesIterator: Iterator[B] = new AbstractIterator[B] {
    private[this] var count = 0

    override def hasNext: Boolean = count < list.size()

    override def next(): B = {
      val b = list.get(count)._2
      count += 1
      b
    }
  }

  override def equals(obj: Any): Boolean = {
    if (super.equals(obj)) return true
    obj match {
      case value: ListLikeMap[_, _] => this.list == value.list
      case _ => false
    }
  }
}

object ListLikeMap {
  def apply[A, B](e: (A, B)*): ListLikeMap[A, B] = {
    val list = new util.ArrayList[(A, B)](e.size)
    val set = scala.collection.mutable.Set.empty[A]
    e.foreach { t =>
      if (set.add(t._1))
        list.add(t)
    }
    new ListLikeMap(list)
  }

  private object EMPTY extends ListLikeMap[Any, Any](new util.ArrayList[(Any, Any)](0)) {
    override def updated[B1 >: Any](key: Any, value: B1, reuseInternalObjects: Boolean): ListLikeMap[Any, B1] = {
      val list = new util.ArrayList[(Any, B1)]
      list.add((key, value))
      new ListLikeMap(list)
    }

    override def remove(key: Any, reuseInternalObjects: Boolean): ListLikeMap[Any, Any] = this
  }

  def empty[A, B]: ListLikeMap[A, B] = EMPTY.asInstanceOf[ListLikeMap[A, B]]

  //noinspection DuplicatedCode
  def main(args: Array[String]): Unit = {
    // Test code
    println("Test ListLikeMap Objects.")
    require(ListLikeMap.empty[String, Int].isEmpty, "Empty instance must be empty.")
    val m1 = ListLikeMap("5" -> 5, "a" -> 1, "1" -> 1, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2)
    require(m1.size == 7, "Size works fine.")
    val m1_1 = m1.updated("1", 11)
    require(m1 == ListLikeMap("5" -> 5, "a" -> 1, "1" -> 1, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2))
    require(m1_1 == ListLikeMap("5" -> 5, "a" -> 1, "1" -> 11, "b" -> 2, "3" -> 3, "c" -> 3, "2" -> 2))
    println("OK.")
  }
}
