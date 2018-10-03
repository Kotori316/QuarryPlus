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

import cofh.api.energy.IEnergyReceiver;
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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.Optional;

/*import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;*/

@Optional.InterfaceList(value = {
    @Optional.Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = QuarryPlus.Optionals.COFH_energy),
    @Optional.Interface(iface = "buildcraft.api.tiles.IDebuggable", modid = QuarryPlus.Optionals.Buildcraft_tiles),
    @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = QuarryPlus.Optionals.IC2_modID)})
public abstract class APowerTile extends APacketTile implements ITickable, IEnergyStorage, IEnergyReceiver, IEnergySink/*, IDebuggable*/ {
    /*package-private*/ double all, maxGot, max, got;
    private boolean ic2ok = false;
    public boolean bcLoaded;
    public final boolean ic2Loaded;
    /**
     * Instance of {@link buildcraft.api.mj.MjCapabilityHelper}. Null if BC isn't installed.
     */
    private Object helper;//buildcraft capability helper
    protected final EnergyDebug debug = new EnergyDebug(this);
    protected boolean outputEnergyInfo = true;

    public APowerTile() {
        bcLoaded = ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.BuildCraft_core);
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
            debug.getAndTick(got);
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
        if (!getWorld().isRemote && !this.ic2ok) {
            if (ic2Loaded) {
                try {
                    ic2load();
                } catch (final Throwable e) {
                    e.printStackTrace();
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
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
            this.ic2ok = false;
        }
    }

    protected final BlockPos[] getNeighbors(EnumFacing facing) {
        return new BlockPos[]{pos.offset(facing), pos.offset(facing.rotateYCCW()), pos.offset(facing.rotateY())};
    }

    public final boolean isOutputEnergyInfo() {
        return outputEnergyInfo;
    }

    public final void toggleOutputEnergyInfo() {
        this.outputEnergyInfo = !this.outputEnergyInfo;
    }

    protected final void startWork() {
        debug.start();
    }

    protected final void finishWork() {
        debug.finish();
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
        outputEnergyInfo = !nbttc.hasKey("outputEnergyInfo") || nbttc.getBoolean("outputEnergyInfo");
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbttc) {
        nbttc.setDouble("storedEnergy", this.all);
        nbttc.setDouble("MAX_stored", this.max);
        nbttc.setDouble("MAX_receive", this.maxGot);
        nbttc.setBoolean("outputEnergyInfo", outputEnergyInfo);
        return super.writeToNBT(nbttc);
    }

    /**
     * Energy Unit is MJ.
     * 1MJ = 2.5EU = 10RF
     *
     * @return the amount of used energy.
     */
    public final double useEnergy(final double min, final double amount, final boolean real, EnergyUsage usage) {
        if (Config.content().noEnergy()) {
            debug.use(amount, !real, usage);
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
        debug.use(res, !real, usage);
        return res;
    }

    /*package-private*/
    final double getEnergy(final double a, final boolean real) {
        if (Config.content().noEnergy()) {
            return 0d;
        }
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
        if (Config.content().noEnergy()) {
            this.all = maxstored;
        }
    }

    //ic2 energy api implecation

    /**
     * Energy unit is EU
     * Accept all EU sent to this machine.
     */
    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public final double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        double mj = amount / 2.5;
        this.got += mj;
        return 0;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
        return canReceive();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public final double getDemandedEnergy() {
        return Math.min(this.maxGot - this.got, this.max - this.all - this.got) * 2.5;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public final int getSinkTier() {
        return 4;
    }

    //cofh(RF) energy api implecation

    /**
     * Energy Unit is RF.
     */
    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_energy)
    public final int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return receiveEnergy(maxReceive, simulate);
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_energy)
    public final int getEnergyStored(EnumFacing from) {
        return getEnergyStored();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_energy)
    public final int getMaxEnergyStored(EnumFacing from) {
        return getMaxEnergyStored();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_energy)
    public final boolean canConnectEnergy(EnumFacing from) {
        return canReceive();
    }

    //Forge energy api implecation

    /**
     * Energy unit is RF.
     */
    @Override
    public final int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;
        int i = (int) getEnergy((double) maxReceive / 10, !simulate) * 10;
        if (maxReceive > 0 && i == 0 && this.max - this.all > 0 && this.max - this.all < 1) {
            return 1;
        }
        return i;
    }

    @Override
    public final int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public final int getEnergyStored() {
        return (int) (this.all * 10);
    }

    @Override
    public final int getMaxEnergyStored() {
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

    /*
     * Energy Unit is micro MJ (1000000 micro MJ = 1 MJ = 0.1 RF)
     *//*
    @Optional.InterfaceList({
            @Optional.Interface(iface = "buildcraft.api.mj.IMjReceiver", modid = QuarryPlus.Optionals.Buildcraft_modID),
            @Optional.Interface(iface = "buildcraft.api.mj.IMjReadable", modid = QuarryPlus.Optionals.Buildcraft_modID)})
    private class MjReciever implements IMjReceiver, IMjReadable {

        @Override
        @Optional.Method(modid = QuarryPlus.Optionals.Buildcraft_modID)
        public long getStored() {
            return (long) (APowerTile.this.getStoredEnergy() * MjAPI.MJ);
        }

        @Override
        @Optional.Method(modid = QuarryPlus.Optionals.Buildcraft_modID)
        public long getCapacity() {
            return (long) (APowerTile.this.getMaxStored() * MjAPI.MJ);
        }

        @Override
        @Optional.Method(modid = QuarryPlus.Optionals.Buildcraft_modID)
        public long getPowerRequested() {
            return (long) (Math.min(APowerTile.this.maxGot - APowerTile.this.got,
                    APowerTile.this.getMaxStored() - APowerTile.this.getStoredEnergy() - APowerTile.this.got) * MjAPI.MJ);
        }

        @Override
        @Optional.Method(modid = QuarryPlus.Optionals.Buildcraft_modID)
        public long receivePower(long microJoules, boolean simulate) {
            return (long) (microJoules - APowerTile.this.getEnergy(microJoules / MjAPI.MJ, !simulate) * MjAPI.MJ);
        }

        @Override
        @Optional.Method(modid = QuarryPlus.Optionals.Buildcraft_modID)
        public boolean canConnect(@Nonnull IMjConnector other) {
            return true;
        }
    }*/

    /*
     * Get the debug information from a tile entity as a list of strings, used for the F3 debug menu. The left and
     * right parameters correspond to the sides of the F3 screen.
     *
     * @param side The side the block was clicked on, may be null if we don't know, or is the "centre" side
     *//*
    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add(getClass().getName());
        left.add(ItemQuarryDebug.tileposToString(this).getText());
        left.add(ItemQuarryDebug.energyToString(this).getText());
        if (isDebugSender) {
            IDebugSender sender = (IDebugSender) this;
            sender.getMessage().stream().map(ITextComponent::getUnformattedComponentText).forEach(left::add);
        }
    }*/

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        /*if (bcLoaded) {
            if (hasMJCapability(capability, facing)) {
                return true;
            }
        }*/
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

   /*@net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    private boolean hasMJCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return ((MjCapabilityHelper) helper).hasCapability(capability, facing);
    }*/

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(this);
        } else {
            /*if (bcLoaded) {
                Object o = getMjCapability(capability, facing);
                if (o != null)
                    return (T) o;
            }*/
            return super.getCapability(capability, facing);
        }
    }

    /*@net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    private <T> Object getMjCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return ((MjCapabilityHelper) helper).getCapability(capability, facing);
    }*/
}
