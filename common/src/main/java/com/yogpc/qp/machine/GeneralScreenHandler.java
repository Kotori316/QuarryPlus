package com.yogpc.qp.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public record GeneralScreenHandler<T extends AbstractContainerMenu>
    (BlockPos pos, Component text, ContainerFactory<T> factory) implements MenuProvider {

    public GeneralScreenHandler(BlockEntity entity, ContainerFactory<T> factory) {
        this(entity.getBlockPos(), entity.getBlockState().getBlock().getName(), factory);
    }

    @Override
    public Component getDisplayName() {
        return text;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return factory.create(i, inventory, pos);
    }

    public interface ContainerFactory<T extends AbstractContainerMenu> {
        T create(int syncId, Inventory inventory, BlockPos pos);
    }
}
