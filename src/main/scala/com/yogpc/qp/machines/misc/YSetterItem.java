package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.QuarryPlacedMessage;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class YSetterItem extends Item implements UseBlockCallback {
    public static final String NAME = "y_setter";

    public YSetterItem() {
        super(new FabricItemSettings().group(QuarryPlus.CREATIVE_TAB));
        UseBlockCallback.EVENT.register(this);
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player.isSpectator() || player.getStackInHand(hand).getItem() != this) return ActionResult.PASS;
        if (world.getBlockEntity(hitResult.getBlockPos()) instanceof TileQuarry quarry) {
            if (!world.isClient) {
                if (player instanceof ServerPlayerEntity p)
                    PacketHandler.sendToClientPlayer(new QuarryPlacedMessage(quarry), p);
                player.openHandledScreen(new YSetterScreenHandler(quarry.getPos(), quarry.getCachedState().getBlock()));
                return ActionResult.CONSUME;
            } else {
                return ActionResult.SUCCESS;
            }
        } else {
            return ActionResult.PASS;
        }
    }
}

class YSetterScreenHandler implements ExtendedScreenHandlerFactory {
    private final BlockPos pos;
    private final Text text;

    public YSetterScreenHandler(BlockPos pos, Block block) {
        this.pos = pos;
        this.text = block.getName();
    }

    @Override
    public Text getDisplayName() {
        return text;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new YSetterContainer(syncId, player, pos);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
