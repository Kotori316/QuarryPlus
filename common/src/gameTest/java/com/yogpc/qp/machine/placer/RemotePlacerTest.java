package com.yogpc.qp.machine.placer;

import com.google.common.base.CaseFormat;
import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class RemotePlacerTest {
    private static final BlockPos placerPos = new BlockPos(2, 2, 2);
    private static final BlockPos targetPos = new BlockPos(4, 4, 4);

    public static Stream<TestFunction> tests(String batchName, String structureName) {
        return Stream.of(
            RemotePlacerTestDirect.removeBlock(batchName, structureName),
            RemotePlacerTestDirect.placeBlock1(batchName, structureName)
        ).flatMap(Function.identity());
    }

    static class RemotePlacerTestDirect {
        private static Stream<TestFunction> removeBlock(String batchName, String structureName) {
            return getBlocks().map(blockName -> {
                var r = Rotation.NONE;
                var name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "RemotePlacerTest_removeBlock_%s_%s".formatted(
                    r.name().toLowerCase(Locale.ROOT), blockName.getPath()));
                return TestFunction.createWithStructure(QuarryPlus.modID, batchName, name, structureName, g -> removeBlock(g, blockName));
            });
        }

        private static void removeBlock(GameTestHelper helper, ResourceLocation blockName) {
            var block = BuiltInRegistries.BLOCK.getValue(blockName);
            var placerBlock = PlatformAccess.getAccess().registerObjects().remotePlacerBlock().get();
            helper.startSequence()
                .thenExecuteAfter(1, () -> helper.setBlock(placerPos, placerBlock))
                .thenExecuteAfter(1, () -> helper.setBlock(targetPos, block))
                .thenExecuteAfter(1, () -> {
                    BlockEntity entity = helper.getBlockEntity(placerPos, RemotePlacerEntity.class);
                    assertInstanceOf(RemotePlacerEntity.class, entity).targetPos = helper.absolutePos(RemotePlacerTest.targetPos);
                })
                .thenExecuteAfter(1, () -> {
                    RemotePlacerEntity placer = helper.getBlockEntity(placerPos, RemotePlacerEntity.class);
                    placer.breakBlock();
                })
                .thenExecuteAfter(1, () -> helper.assertBlockNotPresent(block, targetPos))
                .thenExecuteAfter(1, () -> {
                    RemotePlacerEntity placer = helper.getBlockEntity(placerPos, RemotePlacerEntity.class);
                    assertEquals(1, placer.countItem(block.asItem()), "Placer should contain removed item");
                })
                .thenSucceed();
        }

        private static Stream<TestFunction> placeBlock1(String batchName, String structureName) {
            return getBlocks().map(blockName -> {
                var r = Rotation.NONE;
                var name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "RemotePlacerTest_placeBlock1_%s_%s".formatted(
                    r.name().toLowerCase(Locale.ROOT), blockName.getPath()));
                return TestFunction.createWithStructure(QuarryPlus.modID, batchName, name, structureName, g -> placeBlock1(g, blockName));
            });
        }

        private static void placeBlock1(GameTestHelper helper, ResourceLocation blockName) {
            var block = BuiltInRegistries.BLOCK.getValue(blockName);
            var placerBlock = PlatformAccess.getAccess().registerObjects().remotePlacerBlock().get();
            helper.startSequence()
                .thenExecuteAfter(1, () -> helper.setBlock(placerPos, placerBlock))
                .thenExecuteAfter(1, () -> {
                    BlockEntity entity = helper.getBlockEntity(placerPos, RemotePlacerEntity.class);
                    assertInstanceOf(RemotePlacerEntity.class, entity).targetPos = helper.absolutePos(RemotePlacerTest.targetPos);
                })
                .thenExecuteAfter(1, () -> {
                    RemotePlacerEntity placer = helper.getBlockEntity(placerPos, RemotePlacerEntity.class);
                    placer.setItem(0, new ItemStack(block));
                })
                .thenExecuteAfter(1, () -> {
                    RemotePlacerEntity placer = helper.getBlockEntity(placerPos, RemotePlacerEntity.class);
                    placer.placeBlock();
                })
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(block, RemotePlacerTest.targetPos))
                .thenExecuteAfter(1, () -> {
                    RemotePlacerEntity placer = helper.getBlockEntity(placerPos, RemotePlacerEntity.class);
                    assertEquals(0, placer.countItem(block.asItem()), "Placer must not contain placed item");
                })
                .thenSucceed();
        }
    }

    @NotNull
    private static Stream<ResourceLocation> getBlocks() {
        return Stream.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "stone"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "ice"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "acacia_log"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "birch_leaves"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "obsidian")
        );
    }
}
