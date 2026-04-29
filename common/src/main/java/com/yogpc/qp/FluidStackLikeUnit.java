package com.yogpc.qp;

import com.yogpc.qp.machine.MachineStorage;

public final class FluidStackLikeUnit {
    public static final FluidStackLikeUnit ZERO = new FluidStackLikeUnit(0);
    public static final FluidStackLikeUnit ONE_BUCKET = new FluidStackLikeUnit(MachineStorage.ONE_BUCKET);
    private final long commonAmount;

    FluidStackLikeUnit(long commonAmount) {
        this.commonAmount = commonAmount;
    }

    public long commonAmount() {
        return commonAmount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FluidStackLikeUnit) obj;
        return this.commonAmount == that.commonAmount;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(commonAmount);
    }

    @Override
    public String toString() {
        return "FluidStackLikeUnit[" +
            "commonAmount=" + commonAmount + ']';
    }

    public long fabricAmount() {
        return commonAmount;
    }

    public int forgeAmount() {
        return Math.clamp(commonAmount * 1000L / MachineStorage.ONE_BUCKET, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public int neoForgeAmount() {
        return this.forgeAmount();
    }

    public static FluidStackLikeUnit fromCommon(long amount) {
        return new FluidStackLikeUnit(amount);
    }

    public static FluidStackLikeUnit fromFabric(long amount) {
        return new FluidStackLikeUnit(amount);
    }

    public static FluidStackLikeUnit fromForge(int amount) {
        return new FluidStackLikeUnit((long) amount * MachineStorage.ONE_BUCKET / 1000L);
    }

    public static FluidStackLikeUnit fromNeoForge(int amount) {
        return fromForge(amount);
    }
}
