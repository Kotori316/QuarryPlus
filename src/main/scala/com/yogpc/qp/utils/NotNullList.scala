package com.yogpc.qp.utils

import java.util.Objects

import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

import scala.collection.JavaConverters._
import scala.collection.generic.Clearable
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class NotNullList(val seq: mutable.Buffer[ItemStack] with Clearable) extends NonNullList[ItemStack](seq.asJava, null) {
  var fix = false
  val fixing: ArrayBuffer[ItemStack] = ArrayBuffer.empty[ItemStack]

  override def clear(): Unit = {
    seq.clear()
    fix = false
    fixing.clear()
  }

  override def add(e: ItemStack): Boolean = {
    seq.append(Objects.requireNonNull(e))
    if (fix) fixing += e
    true
  }

  override def add(i: Int, e: ItemStack): Unit = {
    seq.insert(i, Objects.requireNonNull(e))
    if (fix) fixing += e
  }

  override def set(i: Int, e: ItemStack): ItemStack = {
    if (fix) fixing += e
    super.set(i, e)
  }
}
