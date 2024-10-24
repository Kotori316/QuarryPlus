package com.yogpc.qp.machine;

import com.electronwill.nightconfig.core.Config;
import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.PlatformAccessDelegate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PowerEntityTest extends BeforeMC {
    private static class Entity extends PowerEntity {
        protected Entity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
            super(type, pos, blockState);
        }

        @Override
        public void updateMaxEnergyWithEnchantment(Level level) {
        }

        @Override
        protected String getMachineName(BlockEntityType<?> type) {
            return "test";
        }

        @Override
        public boolean isValidBlockState(BlockState blockState) {
            return true;
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
        PlatformAccess.config().enableMap().set("test", true);

        var e = instance();
        e.setMaxEnergy(1000);

        assertAll(
            () -> assertEquals(0, e.getEnergy()),
            () -> assertEquals(0, e.useEnergy(100, true, false, "test")),
            () -> assertEquals(100, e.addEnergy(100, true)),
            () -> assertTrue(e.enabled),
            () -> assertFalse(e.hasEnoughEnergy())
        );
    }

    @Test
    void noEnergyOn() {
        var delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
        var c = Config.inMemory();
        c.set("noEnergy", true);
        delegate.setConfig(c, false);
        PlatformAccess.config().enableMap().set("test", true);

        var e = instance();
        e.setMaxEnergy(1000);
        assertAll(
            () -> assertEquals(1000, e.getEnergy()),
            () -> assertEquals(100, e.useEnergy(100, true, false, "test")),
            () -> assertEquals(0, e.addEnergy(100, true)),
            () -> assertTrue(e.enabled),
            () -> assertTrue(e.hasEnoughEnergy())
        );
    }
}
