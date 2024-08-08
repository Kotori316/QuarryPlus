package com.yogpc.qp.fabric;

import com.yogpc.qp.machine.GeneralScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public record ExtendedGeneralScreenHandler<T extends AbstractContainerMenu>(
    GeneralScreenHandler<T> handler
) implements ExtendedScreenHandlerFactory<BlockPos> {
    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return handler.pos();
    }

    @Override
    public Component getDisplayName() {
        return handler.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return handler.createMenu(i, inventory, player);
    }
}
