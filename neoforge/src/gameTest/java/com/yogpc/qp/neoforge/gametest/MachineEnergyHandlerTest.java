package com.yogpc.qp.neoforge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class MachineEnergyHandlerTest {
    static List<TestFunction> tests() {
        var instance = new MachineEnergyHandlerTest();
        return List.of(
            TestFunction.create(QuarryPlus.modID, "loadHandler", instance::loadHandler),
            TestFunction.create(QuarryPlus.modID, "insertThanMax", instance::insertThanMax),
            TestFunction.create(QuarryPlus.modID, "insertThanMax2", instance::insertThanMax2)
        );
    }

    private static Pair<QuarryEntity, EnergyHandler> getHandler(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(pos, QuarryEntity.class);

        var handler = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(pos), null, quarry, null);
        assertNotNull(handler);
        return Pair.of(quarry, handler);
    }

    public void loadHandler(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var pair = getHandler(helper, pos);
        QuarryEntity quarry = pair.getKey();
        var handler = pair.getValue();

        assertEquals(PlatformAccess.config().powerMap().quarry().maxEnergy(), handler.getCapacityAsLong());

        try (var tx = Transaction.openRoot()) {
            handler.insert(1000, tx);
        }
        assertEquals(0, quarry.getEnergy());
        try (var tx = Transaction.openRoot()) {
            handler.insert(1000, tx);
            tx.commit();
        }
        assertEquals(1000 * PowerEntity.ONE_FE, quarry.getEnergy());

        helper.succeed();
    }

    public void insertThanMax(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var pair = getHandler(helper, pos);
        QuarryEntity quarry = pair.getKey();
        var handler = pair.getValue();

        quarry.setEnergy((long) PlatformAccess.config().powerMap().quarry().maxEnergy() * PowerEntity.ONE_FE, false);

        assertEquals(PlatformAccess.config().powerMap().quarry().maxEnergy(), handler.getCapacityAsLong());

        {
            int inserted;
            try (var tx = Transaction.openRoot()) {
                inserted = handler.insert(1000, tx);
                tx.commit();
            }
            assertEquals(10000 * PowerEntity.ONE_FE, quarry.getEnergy());
            assertEquals(0, inserted, "Quarry should not receive energy than its capacity");
        }
        {
            int inserted;
            try (var tx = Transaction.openRoot()) {
                inserted = handler.insert(1000, tx);
            }
            assertEquals(10000 * PowerEntity.ONE_FE, quarry.getEnergy());
            assertEquals(0, inserted, "Quarry should not receive energy than its capacity");
        }

        helper.succeed();
    }

    public void insertThanMax2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var pair = getHandler(helper, pos);
        var handler = pair.getValue();

        int inserted;
        try (var tx = Transaction.openRoot()) {
            inserted = handler.insert(20000, tx);
            tx.commit();
        }
        assertEquals(10000, inserted, "Quarry should not receive energy than its capacity");

        helper.succeed();
    }
}
