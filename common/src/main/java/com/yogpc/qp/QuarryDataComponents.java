package com.yogpc.qp;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

public final class QuarryDataComponents {
    public static final DataComponentType<Boolean> QUARRY_REMOVE_BEDROCK_COMPONENT = DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
    public static final DataComponentType<Integer> HOLDING_EXP_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build();
}
