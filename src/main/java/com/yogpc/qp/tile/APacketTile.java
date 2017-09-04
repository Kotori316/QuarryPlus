/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.tile;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;


public abstract class APacketTile extends TileEntity {
    /*
    public abstract void S_recievePacket(byte id, byte[] data, EntityPlayer ep);

    public abstract void C_recievePacket(byte id, byte[] data, EntityPlayer ep);

    @Override
    public final Packet getDescriptionPacket() {
        final PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeByte(0);
        new YogpstopPacket(this).writeData(buf);
        return new FMLProxyPacket(buf, "QuarryPlus");
    }*/

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }
}
