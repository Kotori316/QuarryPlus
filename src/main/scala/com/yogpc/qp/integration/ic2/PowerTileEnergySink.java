package com.yogpc.qp.integration.ic2;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

record PowerTileEnergySink(PowerTile tile) implements IEnergySink {
    @Override
    public Level getWorldObj() {
        return tile.getLevel();
    }

    @Override
    public BlockPos getPosition() {
        return tile.getBlockPos();
    }

    @Override
    public int getSinkTier() {
        // Temporal value, 32768 EU/t
        return 6;
    }

    @Override
    public int getRequestedEnergy() {
        var canAcceptNanoFE = tile.getMaxEnergy() - tile.getEnergy();
        var canAcceptEU = canAcceptNanoFE / QuarryPlus.config.powerMap.ic2ConversionRate.get();
        return (int) Mth.clamp(0, canAcceptEU, Integer.MAX_VALUE);
    }

    /**
     * @param direction the direction
     * @param amount    the amount of energy in EU
     * @param voltage   ???
     * @return not accepted energy in EU
     */
    @Override
    public int acceptEnergy(Direction direction, int amount, int voltage) {
        if (tile.getMaxEnergy() - tile.getEnergy() == 0) return 0;
        var energyNanoFE = amount * QuarryPlus.config.powerMap.ic2ConversionRate.get();
        var acceptedNanoFE = tile.addEnergy(energyNanoFE, false);
        var acceptedEU = acceptedNanoFE / QuarryPlus.config.powerMap.ic2ConversionRate.get();
        return amount - (int) acceptedEU;
    }

    /**
     * @param emitter the emitter which tries to connect to this machine.
     * @param side    the direction
     * @return whether this tile can accept energy from emitter and the side.
     */
    @Override
    public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
        return true;
    }
}
