package com.yogpc.qp.container;

import java.util.ArrayList;
import java.util.List;

import com.yogpc.qp.BlockData;
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

    public ContainerEnchList(final TileBasic tq) {
        this.tile = tq;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer ep) {
        return ep.getDistanceSqToCenter(tile.getPos()) <= 64.0D;
    }


    public List<BlockData> fortuneList = new ArrayList<>();
    public List<BlockData> silktouchList = new ArrayList<>();
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
        PacketHandler.sendToClient(DiffMessage.create(this, fortuneList, silktouchList), (EntityPlayerMP) listener);
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
