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

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.yogpc.qp.Config;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class APacketTile extends TileEntity {
    public static final BinaryOperator<String> combiner = (s, s2) -> s + ", " + s2;
    public static final Function<String, TextComponentString> toComponentString = TextComponentString::new;
    public static final Predicate<Object> nonNull = Objects::nonNull;
    public static final Consumer<IChunkLoadTile> requestTicket = IChunkLoadTile::requestTicket;

    private final ITextComponent displayName;
    protected final boolean machineDisabled;

    protected APacketTile() {
        if (HasInv.class.isInstance(this)) {
            HasInv hasInv = (HasInv) this;
            displayName = new TextComponentTranslation(hasInv.getName());
        } else if (IDebugSender.class.isInstance(this)) {
            IDebugSender sender = (IDebugSender) this;
            displayName = new TextComponentTranslation(sender.getDebugName());
        } else {
            displayName = new TextComponentString("APacketTile");
        }

        machineDisabled = Config.content().disableMapJ().get(getSymbol());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    public boolean hasWorld() {
        return hasWorldObj();
    }

    @Override
    public ITextComponent getDisplayName() {
        return displayName;
    }

    protected abstract scala.Symbol getSymbol();
}
