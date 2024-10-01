package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.Area;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdvActionSyncMessageTest {

    @Test
    void createInstance() {
        assertDoesNotThrow(() -> new AdvActionSyncMessage(BlockPos.ZERO, Level.OVERWORLD, new Area(BlockPos.ZERO, BlockPos.ZERO, Direction.UP), WorkConfig.DEFAULT, true));
    }

    @ParameterizedTest
    @MethodSource("messages")
    void cycle(AdvActionSyncMessage message) {
        var buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        message.write(buffer);

        var read = new AdvActionSyncMessage(buffer);
        assertEquals(message, read);
    }

    static Stream<AdvActionSyncMessage> messages() {
        return Stream.of(
            new AdvActionSyncMessage(BlockPos.ZERO, Level.OVERWORLD, new Area(BlockPos.ZERO, BlockPos.ZERO, Direction.UP), WorkConfig.DEFAULT, true),
            new AdvActionSyncMessage(BlockPos.ZERO, Level.OVERWORLD, new Area(BlockPos.ZERO, BlockPos.ZERO, Direction.UP), WorkConfig.DEFAULT, false),
            new AdvActionSyncMessage(new BlockPos(1, 2, 3), Level.NETHER, new Area(BlockPos.ZERO, BlockPos.ZERO, Direction.NORTH), WorkConfig.DEFAULT.noAutoStartConfig(), false),
            new AdvActionSyncMessage(BlockPos.ZERO, Level.OVERWORLD, null, WorkConfig.DEFAULT, true),
            new AdvActionSyncMessage(BlockPos.ZERO, Level.OVERWORLD, null, WorkConfig.DEFAULT, true)
        );
    }
}
