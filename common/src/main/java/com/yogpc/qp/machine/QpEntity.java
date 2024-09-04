package com.yogpc.qp.machine;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.stream.Stream;

public abstract class QpEntity extends BlockEntity {
    public final boolean enabled;

    protected QpEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.enabled = PlatformAccess.config().enableMap().enabled(getMachineName(type));
    }

    protected QpEntity(BlockPos pos, BlockState blockState) {
        this(PlatformAccess.getAccess().registerObjects().getBlockEntityType((QpBlock) blockState.getBlock()).orElseThrow(), pos, blockState);
    }

    protected String getMachineName(BlockEntityType<?> type) {
        var key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        if (key == null) {
            // This block is not registered
            return "invalid";
        }
        return key.getPath();
    }

    public Stream<MutableComponent> checkerLogs() {
        return Stream.of(
            Component.literal("-".repeat(32)),
            Component.empty()
                .append(Component.literal("BlockEntity").withStyle(ChatFormatting.AQUA))
                .append(": %s".formatted(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(getType())))
                .append(" (%s)".formatted(getClass().getSimpleName())),
            detail(ChatFormatting.AQUA, "Enabled", String.valueOf(enabled)),
            detail(ChatFormatting.AQUA, "Pos", getBlockPos().toShortString())
        );
    }

    protected static MutableComponent detail(ChatFormatting color, String title, String content) {
        return Component.empty()
            .append(Component.literal(title).withStyle(color))
            .append(": ")
            .append(content);
    }
}
