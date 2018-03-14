package com.yogpc.qp.container;

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.enchantment.DiffMessage;
import com.yogpc.qp.tile.TileBasic;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;

public class ContainerEnchList extends Container {

    public final TileBasic tile;
    private int includeFlag = 0;

    public ContainerEnchList(TileBasic tile, EntityPlayer player) {
        this.tile = tile;
        if (!tile.getWorld().isRemote)
            PacketHandler.sendToClient(DiffMessage.create(this, tile.fortuneList, tile.silktouchList), ((EntityPlayerMP) player));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSqToCenter(tile.getPos()) <= 64;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        VersionUtil.sendWindowProperty(listener, this, 0, getInclude());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int include = getInclude();
        if (includeFlag != include) {
            includeFlag = include;
            listeners.forEach(listener -> VersionUtil.sendWindowProperty(listener, this, 0, include));
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);
        if (id == 0) {
            this.tile.fortuneInclude = (data & 2) != 0;
            this.tile.silktouchInclude = (data & 1) != 0;
        }
    }

    private int getInclude() {
        int a = tile.fortuneInclude ? 2 : 0;
        int b = tile.silktouchInclude ? 1 : 0;
        return a | b;
    }
}
