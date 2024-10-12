package com.yogpc.qp.forge.gametest;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;

import static com.yogpc.qp.forge.gametest.LoadTest.STRUCTURE;
import static org.junit.jupiter.api.Assertions.*;

@GameTestHolder(QuarryPlus.modID)
public final class MachineEnergyHandlerTest {
    @GameTest(template = STRUCTURE)
    public void loadHandler(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(pos);

        var handler = assertDoesNotThrow(() -> quarry.getCapability(ForgeCapabilities.ENERGY).orElseThrow(AssertionError::new));
        assertNotNull(handler);

        assertEquals(PlatformAccess.config().powerMap().quarry().maxEnergy(), handler.getMaxEnergyStored());

        handler.receiveEnergy(1000, true);
        assertEquals(0, quarry.getEnergy());
        handler.receiveEnergy(1000, false);
        assertEquals(1000 * PowerEntity.ONE_FE, quarry.getEnergy());

        helper.succeed();
    }
}
