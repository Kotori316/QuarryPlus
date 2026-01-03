package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.SoftBlock;
import com.yogpc.qp.machine.storage.DebugStorageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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
        var name = "CheckBlockDrop";
        return Stream.of(
            TestFunction.createWithStructure(QuarryPlus.modID, batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, g -> {
                var pos = BlockPos.ZERO.above();
                var blocks = BuiltInRegistries.BLOCK.entrySet().stream()
                    .filter(e -> e.getKey().identifier().getNamespace().equals(QuarryPlus.modID));
                blocks.forEach(e -> {
                    var b = e.getValue();
                    g.setBlock(pos, b);
                    assertInstanceOf(b.getClass(), g.getBlockState(pos).getBlock());
                    var drops = Block.getDrops(g.getBlockState(pos), g.getLevel(), g.absolutePos(pos), g.getLevel().getBlockEntity(g.absolutePos(pos)));
                    if (EXCEPT.contains(e.getKey().identifier().getPath())) {
                        assertTrue(drops.isEmpty(), "Drop items must be empty for %s".formatted(e.getKey()));
                    } else {
                        assertFalse(drops.isEmpty(), "Drop item is empty for %s".formatted(e.getKey()));
                    }
                    g.setBlock(pos, Blocks.AIR);
                });
                g.succeed();
            })
        );
    }
}
