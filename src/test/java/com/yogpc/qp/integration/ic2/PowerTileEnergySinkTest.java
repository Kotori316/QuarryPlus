package com.yogpc.qp.integration.ic2;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.PowerTileTest;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Wait until IC2 classic is implemented for this version")
@DisabledIfEnvironmentVariable(named = "IGNORE_OTHER_MODS_IN_RUNTIME", matches = ".*")
class PowerTileEnergySinkTest {
    static PowerTile create() {
        TileQuarry quarry = new TileQuarry(BlockPos.ZERO, Holder.BLOCK_QUARRY.defaultBlockState());
        PowerTileTest.setTime(quarry);
        return quarry;
    }

    @Test
    void createInstance() {
        Internal.createInstance();
    }

    @Test
    void emit() {
        Internal.emit();
    }

    @Test
    void getRequired() {
        Internal.getRequired();
    }

    private static class Internal {
        static void createInstance() {
            assertDoesNotThrow(() -> new PowerTileEnergySink(create()));
        }

        static void emit() {
            var sink = new PowerTileEnergySink(create());
            var rest = sink.acceptEnergy(Direction.NORTH, 512, 512);
            assertEquals(0, rest);
            assertEquals(512 * QuarryPlus.config.powerMap.ic2ConversionRate.get(), sink.tile().getEnergy());
        }

        static void getRequired() {
            var sink = new PowerTileEnergySink(create());
            assertTrue(sink.getRequestedEnergy() > 0, "Machine should requests more than 0 EU.");
        }
    }
}
