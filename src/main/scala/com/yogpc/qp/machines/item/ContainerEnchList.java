package com.yogpc.qp.machines.item;

import java.util.Objects;

import com.yogpc.qp.machines.quarry.TileBasic;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.mover.BlockListRequestMessage;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ContainerEnchList extends Container {

    public final TileBasic tile;
    private final IntReferenceHolder includeFlag = this.trackInt(IntReferenceHolder.single());
    public final ResourceLocation enchantmentName;

    public ContainerEnchList(int id, PlayerEntity player, BlockPos pos, ResourceLocation enchantmentName) {
        super(Holder.enchListContainerType(), id);
        this.enchantmentName = enchantmentName;
        this.tile = Objects.requireNonNull(((TileBasic) player.getEntityWorld().getTileEntity(pos)));
        if (!player.world.isRemote) {
            includeFlag.set(getInclude());
        } else {
            PacketHandler.sendToServer(BlockListRequestMessage.create(id));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        BlockPos pos = tile.getPos();
        return playerIn.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= 64;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, getInclude());
    }

    @Override
    public void detectAndSendChanges() {
        includeFlag.set(getInclude());
        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);
        if (id == 0) {
            this.tile.enchantmentFilter =
                this.tile.enchantmentFilter.copy(
                    (data & 2) != 0, // Fortune
                    (data & 1) != 0, // Silktouch
                    tile.enchantmentFilter.fortuneList(),
                    tile.enchantmentFilter.silktouchList()
                );
        }
    }

    private int getInclude() {
        int a = tile.enchantmentFilter.fortuneInclude() ? 2 : 0;
        int b = tile.enchantmentFilter.silktouchInclude() ? 1 : 0;
        return a | b;
    }
}
