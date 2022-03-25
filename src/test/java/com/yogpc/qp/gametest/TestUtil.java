package com.yogpc.qp.gametest;

import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.util.ReflectionUtils;

public final class TestUtil {
    public static final String EMPTY_STRUCTURE = "empty";

    public static BlockPos getBasePos(GameTestHelper helper) {
        return Try.call(() -> GameTestHelper.class.getDeclaredField("testInfo"))
            .andThen(f -> ReflectionUtils.tryToReadFieldValue(f, helper))
            .andThenTry(GameTestInfo.class::cast)
            .andThenTry(GameTestInfo::getStructureBlockPos)
            .andThenTry(helper.getLevel()::getBlockEntity)
            .andThenTry(StructureBlockEntity.class::cast)
            .andThenTry(StructureBlockEntity::getStructurePos)
            .getOrThrow(RuntimeException::new);
    }

    public static TestFunction create(String name, Consumer<GameTestHelper> test) {
        return new TestFunction(
            "defaultBatch", name, QuarryPlus.modID + ":" + EMPTY_STRUCTURE, 100, 0L,
            true, wrapper(test)
        );
    }

    private static Consumer<GameTestHelper> wrapper(Consumer<GameTestHelper> original) {
        return g -> {
            try {
                original.accept(g);
                g.succeed();
            } catch (AssertionError assertionError) {
                var e = new GameTestAssertException(assertionError.getMessage());
                e.addSuppressed(assertionError);
                throw e;
            }
        };
    }
}
