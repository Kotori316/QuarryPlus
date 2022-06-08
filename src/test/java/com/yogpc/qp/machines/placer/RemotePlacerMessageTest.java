package com.yogpc.qp.machines.placer;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.yogpc.qp.QuarryPlusTest;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(QuarryPlusTest.class)
class RemotePlacerMessageTest {
    @Test
    void instance() {
        assertDoesNotThrow(() -> new RemotePlacerMessage(BlockPos.ZERO, Level.OVERWORLD, BlockPos.ZERO));
    }

    static Stream<Arguments> testArguments() {
        var rand = RandomSource.create();
        var poses1 = BlockPos.randomBetweenClosed(rand, 4, -3, -2, -1, 1, 2, 3);
        var poses2 = BlockPos.randomBetweenClosed(rand, 4, -3, -2, -1, 1, 2, 3);
        var dims = List.of(Level.OVERWORLD, Level.NETHER, Level.END);
        return StreamSupport.stream(poses1.spliterator(), false)
            .flatMap(pos -> StreamSupport.stream(poses2.spliterator(), false)
                .flatMap(target -> dims.stream().map(d -> Arguments.of(pos, d, target))));
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    void toPacketTest(BlockPos pos, ResourceKey<Level> dim, BlockPos newTarget) {
        var message = new RemotePlacerMessage(pos, dim, newTarget);
        var packet = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        message.write(packet);
        var reconstructed = new RemotePlacerMessage(packet);
        assertEquals(message, reconstructed);
    }
}
