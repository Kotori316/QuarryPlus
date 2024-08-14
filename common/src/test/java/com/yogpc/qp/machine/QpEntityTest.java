package com.yogpc.qp.machine;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QpEntityTest extends BeforeMC {
    private static class Entity extends QpEntity {
        protected Entity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
            super(type, pos, blockState);
        }

        @Override
        protected String getMachineName(BlockEntityType<?> type) {
            return "test";
        }
    }

    @Test
    void createInstance() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        when(type.isValid(any())).thenReturn(true);

        assertDoesNotThrow(() -> new Entity(type, BlockPos.ZERO, mock(BlockState.class)));
    }

    @Test
    void isEnabled1() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        when(type.isValid(any())).thenReturn(true);
        var entity = new Entity(type, BlockPos.ZERO, mock(BlockState.class));

        assertFalse(entity.enabled);
    }

    @Test
    void isEnabled2() {
        PlatformAccess.config().enableMap().set("test", true);
        BlockEntityType<?> type = mock(BlockEntityType.class);
        when(type.isValid(any())).thenReturn(true);
        var entity = new Entity(type, BlockPos.ZERO, mock(BlockState.class));

        assertTrue(entity.enabled);
    }
}
