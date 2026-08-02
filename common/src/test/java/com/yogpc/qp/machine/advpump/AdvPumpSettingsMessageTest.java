package com.yogpc.qp.machine.advpump;

import io.netty.buffer.ByteBufAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdvPumpSettingsMessageTest {

    @Test
    void createInstance() {
        assertDoesNotThrow(() -> new AdvPumpSettingsMessage(BlockPos.ZERO, Level.OVERWORLD, true, false, false));
    }

    @ParameterizedTest
    @MethodSource("messages")
    void cycle(AdvPumpSettingsMessage message) {
        var buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        message.write(buffer);

        var read = new AdvPumpSettingsMessage(buffer);
        assertEquals(message, read);
    }

    static Stream<AdvPumpSettingsMessage> messages() {
        return Stream.of(
            new AdvPumpSettingsMessage(BlockPos.ZERO, Level.OVERWORLD, true, false, false),
            new AdvPumpSettingsMessage(BlockPos.ZERO, Level.OVERWORLD, false, true, false),
            new AdvPumpSettingsMessage(new BlockPos(1, 2, 3), Level.NETHER, false, false, true),
            new AdvPumpSettingsMessage(new BlockPos(-4, 64, 100), Level.END, true, true, true)
        );
    }
}
