package com.yogpc.qp.forge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.data.GatherGameTest;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.gametest.GameTest;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class MachineEnergyHandlerTest implements GatherGameTest {
    private static Pair<QuarryEntity, IEnergyStorage> getHandler(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(pos, QuarryEntity.class);

        var handler = assertDoesNotThrow(() -> quarry.getCapability(ForgeCapabilities.ENERGY).orElseThrow(AssertionError::new));
        assertNotNull(handler);
        return Pair.of(quarry, handler);
    }

    @GameTest()
    public void loadHandler(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var pair = getHandler(helper, pos);
        QuarryEntity quarry = pair.getKey();
        var handler = pair.getValue();

        assertEquals(PlatformAccess.config().powerMap().quarry().maxEnergy(), handler.getMaxEnergyStored());

        handler.receiveEnergy(1000, true);
        assertEquals(0, quarry.getEnergy());
        handler.receiveEnergy(1000, false);
        assertEquals(1000 * PowerEntity.ONE_FE, quarry.getEnergy());

        helper.succeed();
    }

    @GameTest()
    public void insertThanMax(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var pair = getHandler(helper, pos);
        QuarryEntity quarry = pair.getKey();
        var handler = pair.getValue();

        quarry.setEnergy((long) PlatformAccess.config().powerMap().quarry().maxEnergy() * PowerEntity.ONE_FE, false);

        assertEquals(PlatformAccess.config().powerMap().quarry().maxEnergy(), handler.getEnergyStored());

        {
            var inserted = handler.receiveEnergy(1000, true);
            assertEquals(10000 * PowerEntity.ONE_FE, quarry.getEnergy());
            assertEquals(0, inserted, "Quarry should not receive energy than its capacity");
        }
        {
            var inserted = handler.receiveEnergy(1000, false);
            assertEquals(10000 * PowerEntity.ONE_FE, quarry.getEnergy());
            assertEquals(0, inserted, "Quarry should not receive energy than its capacity");
        }

        helper.succeed();
    }

    @GameTest()
    public void insertThanMax2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var pair = getHandler(helper, pos);
        var handler = pair.getValue();

        var inserted = handler.receiveEnergy(20000, true);
        assertEquals(10000, inserted, "Quarry should not receive energy than its capacity");

        helper.succeed();
    }

    @Override
    public Collection<GameTestProperty> gather() {
        var testData = GameTestProperty.withStructure(Identifier.fromNamespaceAndPath(QuarryPlus.modID, TestFunction.EMPTY_STRUCTURE));
        var prefix = "machine_energy_handler";
        return List.of(
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_load_handler"), testData, this::loadHandler),
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_insert_than_max"), testData, this::insertThanMax),
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_insert_than_max2"), testData, this::insertThanMax2)
        );
    }
}
