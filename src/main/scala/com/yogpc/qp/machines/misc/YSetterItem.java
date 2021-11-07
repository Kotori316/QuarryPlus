package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.QuarryPlacedMessage;
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
        super(new FabricItemSettings().tab(QuarryPlus.CREATIVE_TAB), NAME);
        UseBlockCallback.EVENT.register(this);
    }

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isSpectator() || player.getItemInHand(hand).getItem() != this) return InteractionResult.PASS;
        if (world.getBlockEntity(hitResult.getBlockPos()) instanceof TileQuarry quarry) {
            if (!world.isClientSide) {
                if (player instanceof ServerPlayer p)
                    PacketHandler.sendToClientPlayer(new QuarryPlacedMessage(quarry), p);
                player.openMenu(new YSetterScreenHandler(quarry.getBlockPos(), quarry.getBlockState().getBlock()));
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
