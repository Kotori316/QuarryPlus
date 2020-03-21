package com.yogpc.qp.machines.base

import cats._
import com.google.common.base.Stopwatch
import com.yogpc.qp.Config
import com.yogpc.qp.machines.base.APowerTile.{MJToMicroMJ => mj}

import scala.collection.mutable

class EnergyDebug3(val name: String, val size: Int, tickSupplier: Eval[Long], outputToLog: Eval[Boolean]) {
  private[this] final val usageMap = mutable.Map.empty[EnergyUsage, Long].withDefaultValue(0L)
  private[this] final val stopWatch = Stopwatch.createUnstarted
  private[this] var totalUsed = 0L
  private[this] var uLastTick = 0L
  private[this] var gLastTick = 0L
  private[this] var lastOutput = 0L
  private[this] var startTime = 0L
  private[this] var got: List[Long] = Nil
  private[this] var used: List[Long] = Nil

  def start(): Unit = {
    if (started) return
    stopWatch.start()
    startTime = tickSupplier.value
    lastOutput = startTime
  }

  def get(amount: Long): Unit = {
    val now = tickSupplier.value
    if (gLastTick == now) {
      val g = this.got match {
        case head :: next => (head + amount) :: next
        case Nil => amount :: Nil
      }
      this.got = g
    } else {
      this.got = amount :: this.got
    }
    gLastTick = now
  }

  def use(amount: Long, usage: EnergyUsage): Unit = {
    val now = tickSupplier.value
    if (uLastTick == now) {
      val u = this.used match {
        case head :: next => (head + amount) :: next
        case Nil => amount :: Nil
      }
      this.used = u
    } else {
      this.used = amount :: this.used
    }
    uLastTick = now
    val nb = usageMap.get(usage) match {
      case Some(value) => value + amount
      case None => amount
    }
    usageMap.update(usage, nb)
    totalUsed += amount
  }

  def use(amount: Long, simulate: Boolean, usage: EnergyUsage): Unit =
    if (!simulate) this.use(amount, usage)

  def getAndTick(amount: Long): List[String] = {
    get(amount)
    endTick()
  }

  def finish(): List[String] = {
    if (!started) return Nil
    stopWatch.stop()
    if (outputToLog.value) {
      val now = tickSupplier.value
      val time = Math.max(1, now - this.startTime)
      val tickInfo = printInfo()
      val timeInfo = List(s"$name finished its work and took ${stopWatch.toString}, $time ticks. Used ${totalUsed / mj} MJ at ${totalUsed * 10 / time / mj} RF/t")
      val usageInfo = usageMap.toList.map { case (usage, amount) => s"$usage used ${amount / mj}MJ." }
      usageMap.clear()
      totalUsed = 0L
      startTime = 0L
      uLastTick = 0L
      gLastTick = 0L
      lastOutput = 0L
      stopWatch.reset()
      tickInfo ::: timeInfo ::: usageInfo
    } else {
      stopWatch.reset()
      Nil
    }
  }

  def endTick(): List[String] = {
    if (!started) return Nil
    if (tickSupplier.value - lastOutput >= size) {
      if (outputToLog.value)
        printInfo()
      else
        Nil
    } else {
      Nil
    }
  }

  def printInfo(): List[String] = {
    val allUsed = used.sum / mj
    val allGot = got.sum / mj
    val info = if (allUsed != 0 || allGot != 0) {
      val usedCount = used.size
      val gotCount = got.size
      if (allUsed == 0 || usedCount == 0) {
        if (gotCount == 0)
          List(s"$name used 0 MJ, got 0 MJ")
        else
          List(s"$name used 0 MJ, got $allGot in $gotCount ticks (${allGot * 10 / gotCount} RF/t)")
      } else {
        if (gotCount == 0)
          List(s"$name used $allUsed MJ in $usedCount ticks (${allUsed * 10 / usedCount} RF/t), got 0 MJ")
        else
          List(s"$name used $allUsed MJ in $usedCount ticks (${allUsed * 10 / usedCount} RF/t), got $allGot MJ in $gotCount ticks (${allGot * 10 / gotCount} RF/t)")
      }
    } else Nil
    used = Nil
    got = Nil
    lastOutput = tickSupplier.value
    /*return*/ info
  }

  def started: Boolean = stopWatch.isRunning

  override def toString: String = s"Debugger for $name. $stopWatch t: $totalUsed"

  def getUsageMap: Map[EnergyUsage, Long] = usageMap.toMap
}

object EnergyDebug3 {
  def apply(name: String, size: Int, tile: APowerTile): EnergyDebug3 =
    new EnergyDebug3(name, size,
      Eval.always(tile.getWorld.getGameTime),
      Eval.always(Config.common.debug && tile.isOutputEnergyInfo))
}
