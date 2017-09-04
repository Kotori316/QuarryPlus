package com.yogpc.qp.tile

import com.yogpc.qp.{Config, QuarryPlus}

class EnergyDebug {

    private var count = 0
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
                QuarryPlus.LOGGER.info(
                    s"Quarry used ${allUsed.sum} in ${allUsed.size} ticks, got ${allGot.sum} in 100 ticks."
                )
                getBuilder.clear()
                useBuilder.clear()
            }
        }
    }

    def useEnergy(amount: Double, simulate: Boolean): Unit = {
        if (Config.content.debug && !simulate) {
            useBuilder += amount
        }
    }
}
