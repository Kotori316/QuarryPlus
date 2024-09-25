package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.SoftBlock;
import com.yogpc.qp.machine.storage.DebugStorageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class CheckBlockDropTest {
    private static final Set<String> EXCEPT = Set.of(
        FrameBlock.NAME,
        DebugStorageBlock.NAME,
        SoftBlock.NAME
    );

    public static Stream<TestFunction> checkDrops(String batchName, String structureName) {
        var blocks = BuiltInRegistries.BLOCK.entrySet().stream()
            .filter(e -> e.getKey().location().getNamespace().equals(QuarryPlus.modID));

        return blocks.map(e -> {
            var b = e.getValue();
            var name = "CheckBlockDrop%s".formatted(b.getClass().getSimpleName());
            return new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, 100, 0, true, GameTestFunctions.wrapper(g -> {
                var pos = BlockPos.ZERO.above();
                g.setBlock(pos, b);
                assertInstanceOf(b.getClass(), g.getBlockState(pos).getBlock());
                var drops = Block.getDrops(g.getBlockState(pos), g.getLevel(), g.absolutePos(pos), g.getLevel().getBlockEntity(g.absolutePos(pos)));
                if (EXCEPT.contains(e.getKey().location().getPath())) {
                    assertTrue(drops.isEmpty(), "Drop items must be empty for %s".formatted(e.getKey()));
                } else {
                    assertFalse(drops.isEmpty(), "Drop item is empty for %s".formatted(e.getKey()));
                }
                g.succeed();
            }));
        });
    }
}
