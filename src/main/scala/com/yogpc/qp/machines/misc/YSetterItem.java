package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkHooks;

public class YSetterItem extends QPItem {
    public static final String NAME = "y_setter";

    public YSetterItem() {
        super(new ResourceLocation(QuarryPlus.modID, NAME), new Item.Properties());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        var tile = level.getBlockEntity(context.getClickedPos());
        if (YAccessor.get(tile) != null) {
            if (!level.isClientSide) {
                if (player instanceof ServerPlayer p) {
                    PacketHandler.sendToClientPlayer(new TileMessage(tile), p);
                    NetworkHooks.openScreen(p, new YSetterScreenHandler(tile.getBlockPos(), tile.getBlockState().getBlock()), context.getClickedPos());
                }

                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.SUCCESS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }
}

class YSetterScreenHandler implements MenuProvider {
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
}
