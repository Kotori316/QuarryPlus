package com.yogpc.qp.tile

import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.tileentity.TileEntity

class EnergyDebug(tile: TileEntity) {

    private var count = 0
    private var lastTick = 0l
    private var usedTicks = 0
    private val getBuilder = List.newBuilder[Double]
    private val useBuilder = List.newBuilder[Double]

    def tick(got: Double): Unit = {
        if (Config.content.debug) {
            count += 1
            getBuilder += got
            if (count >= 100) {
                count = 0
                val allGot = getBuilder.result()
                val allUsed = useBuilder.result()
                val gotSum = allGot.sum
                if (allUsed.nonEmpty) {
                    val usedSum = allUsed.sum
                    QuarryPlus.LOGGER.info(
                        s"Quarry used $usedSum in $usedTicks ticks (${usedSum / usedTicks} MJ/t), got $gotSum in 100 ticks (${gotSum / 100} MJ/t)"
                    )
                } else {
                    QuarryPlus.LOGGER.info(
                        s"Quarry used 0 RF, got $gotSum in 100 ticks (${gotSum / 100}MJ/t)"
                    )
                }
                usedTicks = 0
                getBuilder.clear()
                useBuilder.clear()
            }
        }
    }

    def useEnergy(amount: Double, simulate: Boolean): Unit = {
        if (Config.content.debug && !simulate) {
            useBuilder += amount
            if (lastTick != tile.getWorld.getTotalWorldTime) {
                lastTick = tile.getWorld.getTotalWorldTime
                usedTicks += 1
            }
        }
    }
}
