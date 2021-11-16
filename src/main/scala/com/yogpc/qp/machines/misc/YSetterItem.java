package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.QuarryPlacedMessage;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class YSetterItem extends QPItem implements UseBlockCallback {
    public static final String NAME = "y_setter";

    public YSetterItem() {
        super(new FabricItemSettings().tab(QuarryPlus.ModObjects.CREATIVE_TAB), NAME);
        UseBlockCallback.EVENT.register(this);
    }

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isSpectator() || player.getItemInHand(hand).getItem() != this) return InteractionResult.PASS;
        var blockEntity = world.getBlockEntity(hitResult.getBlockPos());
        if (blockEntity != null && YAccessor.get(blockEntity) != null) {
            if (!world.isClientSide) {
                if (blockEntity instanceof TileQuarry quarry && player instanceof ServerPlayer p) {
                    PacketHandler.sendToClientPlayer(new QuarryPlacedMessage(quarry), p);
                } else if (blockEntity instanceof BlockEntityClientSerializable clientSerializable) {
                    clientSerializable.sync();
                }
                player.openMenu(new YSetterScreenHandler(blockEntity.getBlockPos(), blockEntity.getBlockState().getBlock()));
                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.SUCCESS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }
}

class YSetterScreenHandler implements ExtendedScreenHandlerFactory {
    private final BlockPos pos;
    private final Component text;

    public YSetterScreenHandler(BlockPos pos, Block block) {
        this.pos = pos;
        this.text = block.getName();
    }

    @Override
    public Component getDisplayName() {
        return text;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new YSetterContainer(syncId, player, pos);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
