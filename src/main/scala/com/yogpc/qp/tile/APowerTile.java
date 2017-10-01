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

import cofh.redstoneflux.api.IEnergyReceiver;
import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList(value = {
        @Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = QuarryPlus.Optionals.RedstoneFlux_modID),
        @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = QuarryPlus.Optionals.IC2_modID)})
public abstract class APowerTile extends APacketTile implements IEnergyReceiver, IEnergySink, ITickable, IEnergyStorage {
    private double all, maxGot, max, got;
    private boolean ic2ok = false;
    public boolean bcLoaded;
    private Object helper;
    private EnergyDebug debug = new EnergyDebug(this);
    public boolean ic2Loaded;

    public APowerTile() {
        bcLoaded = Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_modID);
        ic2Loaded = Loader.isModLoaded(QuarryPlus.Optionals.IC2_modID);
        /*if (bcLoaded) {
            helper = new MjCapabilityHelper(new MjReciever());
        }*/
    }

    @Override
    public void update() {
        postLoadEvent();
        this.all += this.got;
        if (!getWorld().isRemote && isWorking())
            debug.tick(got);
        this.got = 0;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        postUnLoadEvent();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        postUnLoadEvent();
    }

    private void postLoadEvent() {
        if (!this.ic2ok && !getWorld().isRemote) {
            if (ic2Loaded) {
                try {
                    ic2load();
                } catch (final Throwable ignored) {
                    ignored.printStackTrace();
                }
            }
            this.ic2ok = true;
        }
    }

    private void postUnLoadEvent() {
        if (this.ic2ok && !getWorld().isRemote) {
            if (ic2Loaded) {
                try {
                    ic2unload();
                } catch (final Throwable ignored) {
                    ignored.printStackTrace();
                }
            }
            this.ic2ok = false;
        }
    }

    protected abstract boolean isWorking();

    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public final void ic2load() {
        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
    }

    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public final void ic2unload() {
        MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        setStoredEnergy(nbttc.getDouble("storedEnergy"));
        configure(nbttc.getDouble("MAX_receive"), nbttc.getDouble("MAX_stored"));
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbttc) {
        nbttc.setDouble("storedEnergy", this.all);
        nbttc.setDouble("MAX_stored", this.max);
        nbttc.setDouble("MAX_receive", this.maxGot);
        return super.writeToNBT(nbttc);
    }

    /**
     * Energy Unit is MJ.
     * 1MJ = 2.5EU = 10RF
     *
     * @return the amount of used energy.
     */
    public final double useEnergy(final double min, final double amount, final boolean real) {
        if (Config.content().noEnergy()) {
            debug.useEnergy(amount, !real);
            return amount;
        }
        double res = 0;
        if (this.all >= min) {
            if (this.all <= amount) {
                res = this.all;
                if (real)
                    this.all = 0;
            } else {
                res = amount;
                if (real)
                    this.all -= amount;
            }
        }
        debug.useEnergy(res, !real);
        return res;
    }

    private double getEnergy(final double a, final boolean real) {
        final double ret = Math.min(Math.min(this.maxGot - this.got, this.max - this.all - this.got), a);
        if (real)
            this.got += ret;
        return ret;
    }

    public final double getStoredEnergy() {
        return this.all;
    }

    public final void setStoredEnergy(double all) {
        this.all = all;
    }

    public final double getMaxStored() {
        return this.max;
    }

    public final void configure(final double maxRecieve, final double maxstored) {
        this.maxGot = maxRecieve;
        this.max = maxstored;
    }

    //ic2 energy api implecation

    /**
     * Energy unit is EU
     */
    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        return amount - getEnergy(amount / 2.5, true) * 2.5;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
        return true;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public final double getDemandedEnergy() {
        return Math.min(this.maxGot - this.got, this.max - this.all - this.got) * 2.5;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public final int getSinkTier() {
        return 3;
    }

    //cofh(RF) energy api implecation

    /**
     * Energy Unit is RF.
     */
    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.RedstoneFlux_modID)
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return receiveEnergy(maxReceive, simulate);
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.RedstoneFlux_modID)
    public int getEnergyStored(EnumFacing from) {
        return getEnergyStored();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.RedstoneFlux_modID)
    public int getMaxEnergyStored(EnumFacing from) {
        return getMaxEnergyStored();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.RedstoneFlux_modID)
    public boolean canConnectEnergy(EnumFacing from) {
        return canReceive();
    }

    //Forge energy api implecation

    /**
     * Energy unit is RF.
     */
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) getEnergy((double) maxReceive / 10, !simulate) * 10;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) (this.all * 10);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) (this.max * 10);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    //Buildcraft MJ energy api implecation

    /**
     * Energy Unit is micro MJ (1000000 micro MJ = 1 MJ = 0.1 RF)
     */
    /*
    @Optional.InterfaceList({
            @Optional.Interface(iface = "buildcraft.api.mj.IMjReceiver", modid = QuarryPlus.Optionals.Buildcraft_modID),
            @Optional.Interface(iface = "buildcraft.api.mj.IMjReadable", modid = QuarryPlus.Optionals.Buildcraft_modID)})
    private class MjReciever implements IMjReceiver, IMjReadable {

        @Override
        public long getStored() {
            return (long) (APowerTile.this.getStoredEnergy() * MjAPI.MJ);
        }

        @Override
        public long getCapacity() {
            return (long) (APowerTile.this.getMaxStored() * MjAPI.MJ);
        }

        @Override
        public long getPowerRequested() {
            return (long) (Math.min(APowerTile.this.maxGot - APowerTile.this.got,
                    APowerTile.this.getMaxStored() - APowerTile.this.getStoredEnergy() - APowerTile.this.got) * MjAPI.MJ);
        }

        @Override
        public long receivePower(long microJoules, boolean simulate) {
            return (long) (microJoules - APowerTile.this.getEnergy(microJoules / MjAPI.MJ, !simulate) * MjAPI.MJ);
        }

        @Override
        public boolean canConnect(@Nonnull IMjConnector other) {
            return true;
        }
    }*/
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        /*if (bcLoaded) {
            if (((MjCapabilityHelper) helper).hasCapability(capability, facing)) {
                return true;
            }
        }*/
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(this);
        } else {
            /*if (bcLoaded) {
                Object o = ((MjCapabilityHelper) helper).getCapability(capability, facing);
                if (o != null)
                    return (T) o;
            }*/
            return super.getCapability(capability, facing);
        }
    }
}
