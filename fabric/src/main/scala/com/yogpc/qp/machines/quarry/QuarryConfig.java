package com.yogpc.qp.machines.quarry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

record QuarryConfig(boolean removeFluid) {
    static QuarryConfig fromTag(CompoundTag tag) {
        // Default true
        boolean removeFluid = !tag.contains("removeFluid") || tag.getBoolean("removeFluid");
        return new QuarryConfig(
            removeFluid
        );
    }

    CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("removeFluid", this.removeFluid);
        return tag;
    }

    static QuarryConfig fromPacket(FriendlyByteBuf buf) {
        boolean removeFluid = buf.readBoolean();
        return new QuarryConfig(
            removeFluid
        );
    }

    void writePacket(FriendlyByteBuf buf) {
        buf.writeBoolean(this.removeFluid);
    }

    QuarryConfig toggleRemoveFluid() {
        return new QuarryConfig(
            !this.removeFluid
        );
    }
}
