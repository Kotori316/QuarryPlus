package com.yogpc.qp.tile

import com.yogpc.qp.QuarryPlus
import net.minecraft.tileentity.TileEntity

import scala.collection.mutable.ListBuffer

class EnergyDebug(tile: TileEntity) {

    private var count = 0
    private var lastTick = 0l
    private var usedTicks = 0
    private var mMaxUsed = 0
    private val getBuilder = new ListBuffer[Double]
    private val useBuilder = new ListBuffer[Double]
    private val tilename = tile.getClass.getSimpleName

    def tick(got: Double): Unit = {
        if (true /*Config.content.debug*/ ) {
            count += 1
            if (getBuilder.size < 100) {
                getBuilder += got
            } else {
                // keep size 100
                getBuilder.remove(0)
                getBuilder += got
            }

            if (count >= 100) {
                count = 0
                val allGot = getBuilder
                val allUsed = useBuilder
                val gotSum = allGot.sum
                if (allUsed.nonEmpty && usedTicks != 0) {
                    val usedSum = allUsed.sum
                    QuarryPlus.LOGGER.info(
                        s"$tilename used $usedSum MJ in $usedTicks ticks (${usedSum / usedTicks} MJ/t), got $gotSum in 100 ticks (${gotSum / 100} MJ/t)"
                    )
                } else {
                    useBuilder.clear()
                    QuarryPlus.LOGGER.info(
                        s"$tilename used 0 MJ, got $gotSum in 100 ticks (${gotSum / 100} MJ/t)"
                    )
                }
                usedTicks = 0
            }
        }
    }

    def useEnergy(amount: Double, simulate: Boolean): Unit = {
        if ( /*Config.content.debug &&*/ !simulate) {
            if (useBuilder.size < 100) {
                useBuilder += amount
            } else {
                // keep size 100
                useBuilder.remove(0)
                useBuilder += amount
            }
            if (mMaxUsed < amount) {
                mMaxUsed = amount.toInt
            }
            if (lastTick != tile.getWorld.getTotalWorldTime) {
                lastTick = tile.getWorld.getTotalWorldTime
                usedTicks += 1
            }
        }
    }

    def maxUsed = mMaxUsed

    def energyPerTick: Int = {
        if (useBuilder.isEmpty) {
            0
        } else {
            val allgot = useBuilder.sum
            (allgot / useBuilder.size).toInt
        }
    }
}
