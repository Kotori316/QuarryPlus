package com.yogpc.qp.machine.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

public final class DigMinY {
    private boolean isSet = false;
    private int minY;

    public DigMinY() {
    }

    public DigMinY(boolean isSet, int minY) {
        this.isSet = isSet;
        this.minY = minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
        isSet = true;
    }

    public int getMinY(@Nullable LevelReader level) {
        if (isSet) {
            return minY;
        }
        if (level == null) {
            return 0;
        }
        return level.getMinBuildHeight() + 1;
    }

    public static final MapCodec<DigMinY> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(t -> t.isSet, "isSet", Codec.BOOL),
        RecordCodecBuilder.of(t -> t.minY, "minY", Codec.INT)
    ).apply(i, DigMinY::new));
}
