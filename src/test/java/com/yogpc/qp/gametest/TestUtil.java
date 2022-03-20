package com.yogpc.qp.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.util.ReflectionUtils;

final class TestUtil {
    static final String EMPTY_STRUCTURE = "empty";

    static BlockPos getBasePos(GameTestHelper helper) {
        return Try.call(() -> GameTestHelper.class.getDeclaredField("testInfo"))
            .andThen(f -> ReflectionUtils.tryToReadFieldValue(f, helper))
            .andThenTry(GameTestInfo.class::cast)
            .andThenTry(GameTestInfo::getStructureBlockPos)
            .andThenTry(helper.getLevel()::getBlockEntity)
            .andThenTry(StructureBlockEntity.class::cast)
            .andThenTry(StructureBlockEntity::getStructurePos)
            .getOrThrow(RuntimeException::new);
    }
}
