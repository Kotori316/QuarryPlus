package com.yogpc.qp.machine.misc;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QuarryChunkLoaderTest {
    @ParameterizedTest
    @MethodSource("provider")
    void cycleNone(QuarryChunkLoader loader) {
        var encoded = assertDoesNotThrow(() -> QuarryChunkLoader.CODEC.encodeStart(JsonOps.INSTANCE, loader).getOrThrow());
        var decoded = assertDoesNotThrow(() -> QuarryChunkLoader.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertEquals(loader, decoded);
    }

    static Stream<QuarryChunkLoader> provider() {
        return Stream.of(
            QuarryChunkLoader.None.INSTANCE,
            new QuarryChunkLoader.Load(BlockPos.ZERO),
            new QuarryChunkLoader.Load(new BlockPos(100, 0, 100)),
            new QuarryChunkLoader.Load(new BlockPos(-100, 0, -100))
        );
    }
}
