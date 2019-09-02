package com.yogpc.qp.machines.base

import com.google.common.base.Stopwatch
import com.yogpc.qp.{Config, QuarryPlus}

import scala.collection.mutable

class EnergyDebug(tile: APowerTile) {

  private[this] val tileName = tile.getClass.getSimpleName
  private[this] val got = Array.ofDim[Long](100)
  private[this] val used = Array.ofDim[Long](100)
  private[this] var gotCount = 0
  private[this] var usedCount = 0
  private[this] var totalUsed = 0L

  private[this] var uLastTick = 0L
  private[this] var gLastTick = 0L
  private[this] var lastOutput = 0L
  private[this] final val mj = APowerTile.MJToMicroMJ
  private[this] val stopWatch = Stopwatch.createUnstarted()
  private[this] var startTime = 0L
  private[this] val usageMap = mutable.Map.empty[EnergyUsage, Long]

  def started: Boolean = stopWatch.isRunning

  def start(): Unit = {
    if (started) return
    stopWatch.start()
    startTime = getTime
  }

  private def getTime: Long = {
    tile.getWorld.getGameTime
  }

  private def outputInfo: Boolean = {
    Config.common.debug && tile.isOutputEnergyInfo
  }

  def use(amount: Long, simulate: Boolean, usage: EnergyUsage): Unit = {
    if (!outputInfo || simulate) return
    if (!started)
      start()
    val tick = getTime
    val energy = Math.round(amount)
    if (tick == uLastTick) {
      used(usedCount - 1) += energy
    } else {
      usedCount += 1
      if (usedCount <= 100)
        used(usedCount - 1) = energy
      uLastTick = tick
    }
    totalUsed += energy
    usageMap(usage) = usageMap.getOrElse(usage, 0L) + energy
  }

  def get(amount: Long): Unit = {
    if (!outputInfo) return
    val tick = getTime
    val energy = Math.round(amount)
    if (tick == gLastTick) {
      if (gotCount > 0)
        got(gotCount - 1) += energy
    } else {
      gotCount += 1
      if (gotCount > 100) {
        print(gotCount)
        return
      }
      got(gotCount - 1) = energy
      gLastTick = tick
    }
  }

  def tick(): Unit = {
    if (getTime - lastOutput >= 100) {
      if (lastOutput == 0L) {
        usedCount = 0
        gotCount = 0
        lastOutput = getTime
        //        gLastTick = 0
      } else if (outputInfo) {
        printInfo()
      }
    }
  }

  private def printInfo(): Unit = {
    val allUsed = used.take(usedCount).sum / mj
    val allGot = got.take(gotCount).sum / mj
    if (allUsed != 0 || allGot != 0) {
      if (allUsed == 0 || usedCount == 0) {
        if (gotCount == 0)
          QuarryPlus.LOGGER.info(s"$tileName used 0 MJ, got 0 MJ")
        else
          QuarryPlus.LOGGER.info(
            s"$tileName used 0 MJ, got $allGot in 100 ticks (${allGot * 10 / gotCount} RF/t)"
          )
      } else {
        if (gotCount == 0)
          QuarryPlus.LOGGER.info(s"$tileName used $allUsed MJ in $usedCount ticks (${allUsed * 10 / usedCount} RF/t), got 0 MJ")
        else
          QuarryPlus.LOGGER.info(
            s"$tileName used $allUsed MJ in $usedCount ticks (${allUsed * 10 / usedCount} RF/t), got $allGot in $gotCount ticks (${allGot * 10 / gotCount} RF/t)"
          )
      }
    }
    usedCount = 0
    gotCount = 0
    lastOutput = getTime
  }

  def getAndTick(amount: Long): Unit = {
    get(amount)
    tick()
  }

  def finish(): Unit = {
    if (!started) return
    stopWatch.stop()
    if (outputInfo) {
      printInfo()
      val time = if(getTime - startTime > 0) getTime - startTime else 1
      QuarryPlus.LOGGER.info(
        s"$tileName finished its work and took ${stopWatch.toString}, $time ticks. Used ${totalUsed / mj} MJ at ${totalUsed * 10 / time / mj} RF/t"
      )
      usageMap.foreach { case (usage, amount) => QuarryPlus.LOGGER.info(usage + " used " + amount / mj + "MJ.") }
      usageMap.clear()
      totalUsed = 0L
      startTime = 0L
      uLastTick = 0L
      gLastTick = 0L
      lastOutput = 0L
    }
    stopWatch.reset()
  }

  override def toString: String = {
    s"Debugger for $tileName. $stopWatch t: $totalUsed"
  }
}
