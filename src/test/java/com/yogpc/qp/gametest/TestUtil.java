package com.yogpc.qp.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.junit.jupiter.api.Assertions;

final class TestUtil {
    static final String EMPTY_STRUCTURE = "empty";

    static BlockPos getBasePos(GameTestHelper helper) {
        try {
            var f = GameTestHelper.class.getDeclaredField("testInfo");
            f.setAccessible(true);
            var info = (GameTestInfo) f.get(helper);
            var pos = info.getStructureBlockPos();
            var structure = (StructureBlockEntity) helper.getLevel().getBlockEntity(pos);
            Assertions.assertNotNull(structure);
            return structure.getStructurePos();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
