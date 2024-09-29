package com.yogpc.qp.machine.advquarry;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class WorkConfigTest {
    static Stream<WorkConfig> configStream() {
        return Stream.of(
            WorkConfig.DEFAULT,
            new WorkConfig(false, true, true),
            new WorkConfig(true, true, true),
            new WorkConfig(false, false, false)
        );
    }

    @Nested
    class PacketTest {
        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machine.advquarry.WorkConfigTest#configStream")
        void cycle(WorkConfig config) {
            var buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
            config.writePacket(buffer);
            var read = new WorkConfig(buffer);
            assertEquals(config, read);
        }
    }

    @Nested
    class CodecTest {
        @ParameterizedTest
        @MethodSource("com.yogpc.qp.machine.advquarry.WorkConfigTest#configStream")
        void cycle(WorkConfig config) {
            var json = assertDoesNotThrow(() -> WorkConfig.CODEC.codec().encodeStart(JsonOps.INSTANCE, config).getOrThrow());
            var read = assertDoesNotThrow(() -> WorkConfig.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow());
            assertEquals(config, read);
        }
    }

    @ParameterizedTest
    @MethodSource("configStream")
    void startSoonConfigTest(WorkConfig config) {
        var result = config.startSoonConfig();
        assertTrue(result.startImmediately());
    }

    @ParameterizedTest
    @MethodSource("configStream")
    void noAutoStartConfigTest(WorkConfig config) {
        var result = config.noAutoStartConfig();
        assertFalse(result.startImmediately());
    }
}
