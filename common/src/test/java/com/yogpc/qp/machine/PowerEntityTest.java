package com.yogpc.qp.machine;

import com.electronwill.nightconfig.core.Config;
import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.PlatformAccessDelegate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PowerEntityTest extends BeforeMC {
    private static class Entity extends PowerEntity {
        protected Entity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
            super(type, pos, blockState);
        }

        @Override
        protected String getMachineName(BlockEntityType<?> type) {
            return "test";
        }
    }

    @BeforeEach
    void setUp() {
        var delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
        delegate.reset();
    }

    static Entity instance() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        when(type.isValid(any())).thenReturn(true);
        var entity = new Entity(type, BlockPos.ZERO, mock(BlockState.class));
        entity.setTimeProvider(() -> 1L);
        return entity;
    }

    @Test
    void createInstance() {
        assertDoesNotThrow(PowerEntityTest::instance);
    }

    @Test
    void noEnergyOff() {
        var delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
        var c = Config.inMemory();
        c.set("noEnergy", false);
        delegate.setConfig(c, false);

        var e = instance();
        e.setMaxEnergy(1000);
        assertEquals(0, e.getEnergy());
        assertEquals(0, e.useEnergy(100, true, false, "test"));
        assertEquals(100, e.addEnergy(100, false));
    }

    @Test
    void noEnergyOn() {
        var delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
        var c = Config.inMemory();
        c.set("noEnergy", true);
        delegate.setConfig(c, false);

        var e = instance();
        e.setMaxEnergy(1000);
        assertEquals(1000, e.getEnergy());
        assertEquals(100, e.useEnergy(100, true, false, "test"));
        assertEquals(0, e.addEnergy(100, false));
    }
}
