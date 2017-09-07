package com.yogpc.qp.container;

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.enchantment.DiffMessage;
import com.yogpc.qp.tile.TileBasic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerEnchList extends Container {
    private final TileBasic tile;

    public ContainerEnchList(final TileBasic tq, EntityPlayer player) {
        this.tile = tq;
        if (!tile.getWorld().isRemote) {
            PacketHandler.sendToClient(DiffMessage.create(this, tile.fortuneList, tile.silktouchList), ((EntityPlayerMP) player));
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer ep) {
        return ep.getDistanceSqToCenter(tile.getPos()) <= 64.0D;
    }

    private byte includeFlag;

    private byte getInclude() {
        return (byte) ((this.tile.fortuneInclude ? 2 : 0) | (this.tile.silktouchInclude ? 1 : 0));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        final byte ninc = getInclude();
        if (this.includeFlag != ninc) {
            this.includeFlag = ninc;
            for (IContainerListener listener : this.listeners)
                listener.sendProgressBarUpdate(this, 0, this.includeFlag);
        }
        for (IContainerListener listener : listeners) {
            PacketHandler.sendToClient(DiffMessage.create(this, tile.fortuneList, tile.silktouchList), ((EntityPlayerMP) listener));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendProgressBarUpdate(this, 0, getInclude());
        PacketHandler.sendToClient(DiffMessage.create(this, tile.fortuneList, tile.silktouchList), (EntityPlayerMP) listener);
    }

    public TileBasic getTile() {
        return tile;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(final int i, final int j) {
        if (i == 0) {
            this.tile.fortuneInclude = (j & 2) != 0;
            this.tile.silktouchInclude = (j & 1) != 0;
        }
    }
}
