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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import com.yogpc.qp.PowerManager;
import com.yogpc.qp.compat.INBTWritable;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.packet.distiller.AnimatonMessage;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

/**
 * See {@link buildcraft.factory.tile.TileDistiller_BC8}, {@link buildcraft.api.recipes.IRefineryRecipeManager}, {@link buildcraft.energy.BCEnergyRecipes}
 */
public class TileRefinery extends APowerTile implements IEnchantableTile {
    public final DistillerTank horizontalsTank = new DistillerTank("horizontalsTank");
    public final DistillerTank upTank = new DistillerTank("upTank");
    public final DistillerTank downTank = new DistillerTank("downTank");
    private List<DistillerTank> tanks = Arrays.asList(horizontalsTank, upTank, downTank);
    private final AllTanks fluidHandler = new AllTanks();

    public long rem_energy;
    public FluidStack cacheIn;
    public FluidStack cachedGas;
    public FluidStack cachedLiqud;
    public long cacheEnergy;

    private DEnch ench = DEnch.defaultEnch;
    public float animationSpeed = 1;
    private int animationStage = 0;

    public TileRefinery() {
        horizontalsTank.setCanDrain(false);
        if (bcLoaded) {
            horizontalsTank.predicate = fluidStack -> BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(fluidStack) != null;
        } else {
            horizontalsTank.predicate = fluidStack -> false;
        }
        upTank.setCanFill(false);
        downTank.setCanFill(false);
    }

    @Override
    public void G_reinit() {
        PowerManager.configureRefinery(this, ench.efficiency, ench.unbreaking);
        tanks.forEach(distillerTank -> distillerTank.setCapacity(ench.getCapacity()));
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        this.ench = DEnch.readFromNBT(nbttc);
        horizontalsTank.readFromNBT(nbttc);
        upTank.readFromNBT(nbttc);
        downTank.readFromNBT(nbttc);
        updateRecipe();
        this.rem_energy = nbttc.getLong("rem_energy");
        this.animationSpeed = nbttc.getFloat("animationSpeed");
        this.animationStage = nbttc.getInteger("animationStage");
        PowerManager.configureRefinery(this, ench.efficiency, ench.unbreaking);
        tanks.forEach(distillerTank -> distillerTank.setCapacity(ench.getCapacity()));
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbttc) {
        ench.writeToNBT(nbttc);
        horizontalsTank.writeToNBT(nbttc);
        upTank.writeToNBT(nbttc);
        downTank.writeToNBT(nbttc);
        nbttc.setLong("rem_energy", this.rem_energy);
        nbttc.setFloat("animationSpeed", this.animationSpeed);
        nbttc.setInteger("animationStage", this.animationStage);
        return super.writeToNBT(nbttc);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            simpleAnimationIterate();
            return;
        }
        if (getWorld().getWorldTime() % 20 == 7)
            PacketHandler.sendToAround(TileMessage.create(this), getWorld(), getPos());
        if (this.cachedGas == null || cachedLiqud == null) {
            decreaseAnimation();
            return;
        }
        double v = MjReciever.getMJfrommicro(cacheEnergy);
        if (cacheIn == null || getStoredEnergy() < v ||
                !PowerManager.useEnergyRefinery(this, v, ench.unbreaking, ench.efficiency)) {
            decreaseAnimation();
        } else {
            increaseAnimation();
            horizontalsTank.drainInternal(cacheIn.amount, true);
            upTank.fillInternal(cachedGas, true);
            downTank.fillInternal(cachedLiqud, true);
            this.cacheIn = null;
            this.cachedGas = null;
            this.cachedLiqud = null;
            this.cacheEnergy = 0L;
            decreaseAnimation();
            updateRecipe();
        }

    }

    @Override
    protected boolean isWorking() {
        return cacheEnergy > 0;
    }

    private void updateRecipe() {
        if (bcLoaded) {
            IRefineryRecipeManager.IDistillationRecipe recipe =
                    BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(horizontalsTank.getFluid());
            if (recipe != null) {
                cacheIn = recipe.in();
                cachedLiqud = recipe.outLiquid();
                cachedGas = recipe.outGas();
                cacheEnergy = recipe.powerRequired();
            }
        }
    }

    public int getAnimationStage() {
        return this.animationStage;
    }

    private void simpleAnimationIterate() {
        if (this.animationSpeed > 1) {
            this.animationStage += this.animationSpeed;

            if (this.animationStage > 300)
                this.animationStage = 100;
        } else if (this.animationStage > 0)
            this.animationStage--;
    }

    private void sendNowPacket() {
        PacketHandler.sendToAround(AnimatonMessage.create(this), getWorld(), getPos());
    }

    private void increaseAnimation() {
        final float prev = this.animationSpeed;
        if (this.animationSpeed < 2)
            this.animationSpeed = 2;
        else if (this.animationSpeed <= 5)
            this.animationSpeed += 0.1;

        this.animationStage += this.animationSpeed;

        if (this.animationStage > 300)
            this.animationStage = 100;
        if (this.animationSpeed != prev)
            sendNowPacket();
    }

    private void decreaseAnimation() {
        final float prev = this.animationSpeed;
        if (this.animationSpeed >= 1) {
            this.animationSpeed -= 0.1;

            this.animationStage += this.animationSpeed;

            if (this.animationStage > 300)
                this.animationStage = 100;
        } else if (this.animationStage > 0)
            this.animationStage--;
        if (this.animationSpeed != prev)
            sendNowPacket();
    }

    @Override
    public Map<Integer, Byte> getEnchantments() {
        final Map<Integer, Byte> ret = new HashMap<>();
        if (ench.efficiency > 0)
            ret.put(EfficiencyID, ench.efficiency);
        if (ench.fortune > 0)
            ret.put(FortuneID, ench.fortune);
        if (ench.unbreaking > 0)
            ret.put(UnbreakingID, ench.unbreaking);
        if (ench.silktouch)
            ret.put(SilktouchID, (byte) 1);
        return ret;
    }

    @Override
    public void setEnchantent(final short id, final short val) {
        ench = ench.copy(id, val);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler handler;
            if (facing == null) {
                handler = fluidHandler;
            } else if (facing == EnumFacing.DOWN) {
                handler = downTank;
            } else if (facing == EnumFacing.UP) {
                handler = upTank;
            } else {
                handler = horizontalsTank;
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    private class AllTanks implements IFluidHandler {

        /**
         * Returns an array of objects which represent the internal tanks.
         * These objects cannot be used to manipulate the internal tanks.
         *
         * @return Properties for the relevant internal tanks.
         */
        @Override
        public IFluidTankProperties[] getTankProperties() {
            IFluidTankProperties[] array = tanks.stream().flatMap(distillerTank -> Stream.of(distillerTank.getTankProperties())).toArray(IFluidTankProperties[]::new);
            if (array.length == 0) {
                return new IFluidTankProperties[]{new FluidTankProperties(null, horizontalsTank.getCapacity(), false, false)};
            } else {
                return array;
            }
        }

        /**
         * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
         *
         * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
         * @param doFill   If false, fill will only be simulated.
         * @return Amount of resource that was (or would have been, if simulated) filled.
         */
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        /**
         * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
         *
         * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
         * @param doDrain  If false, drain will only be simulated.
         * @return FluidStack representing the Fluid and amount that was (or would have been, if
         * simulated) drained.
         */
        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        /**
         * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
         * <p/>
         * This method is not Fluid-sensitive.
         *
         * @param maxDrain Maximum amount of fluid to drain.
         * @param doDrain  If false, drain will only be simulated.
         * @return FluidStack representing the Fluid and amount that was (or would have been, if
         * simulated) drained.
         */
        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }
    }

    private class DistillerTank extends FluidTank {
        private final String name;
        private Predicate<FluidStack> predicate = fluidStack -> false;

        public DistillerTank(String name) {
            super(4 * Fluid.BUCKET_VOLUME);
            this.name = name;
        }

        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            int i = super.fillInternal(resource, doFill);
            updateRecipe();
            return i;
        }

        @Nullable
        @Override
        public FluidStack drainInternal(int maxDrain, boolean doDrain) {
            FluidStack stack = super.drainInternal(maxDrain, doDrain);
            updateRecipe();
            return stack;
        }

        @Override
        public DistillerTank readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt.getCompoundTag(name));
            return this;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            NBTTagCompound compound = new NBTTagCompound();
            super.writeToNBT(compound);
            nbt.setTag(name, compound);
            return nbt;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return predicate.test(fluid) && super.canFillFluidType(fluid);
        }
    }

    private static class DEnch implements INBTWritable {
        private static final String NBT_DEnch = "dench";
        private static final DEnch defaultEnch = new DEnch(0, 0, 0, false);
        private static final int[] CAPACITIES = {4000, 5200, 6760, 8788};
        private final byte efficiency;
        private final byte unbreaking;
        private final byte fortune;
        private final boolean silktouch;

        public DEnch(int efficiency, int unbreaking, int fortune, boolean silktouch) {
            this.efficiency = (byte) efficiency;
            this.unbreaking = (byte) unbreaking;
            this.fortune = (byte) fortune;
            this.silktouch = silktouch;
        }

        public int getCapacity() {
            if (fortune >= 3) {
                return CAPACITIES[3];
            } else {
                return CAPACITIES[fortune];
            }
        }

        public DEnch copy(short id, short val) {
            if (id == EfficiencyID) return new DEnch(val, unbreaking, fortune, silktouch);
            else if (id == FortuneID) return new DEnch(efficiency, unbreaking, val, silktouch);
            else if (id == UnbreakingID) return new DEnch(efficiency, val, fortune, silktouch);
            else if (id == SilktouchID) return new DEnch(efficiency, unbreaking, fortune, val > 0);
            else return this;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            NBTTagCompound t = new NBTTagCompound();
            t.setByte("efficiency", efficiency);
            t.setByte("unbreaking", unbreaking);
            t.setByte("fortune", fortune);
            t.setBoolean("silktouch", silktouch);
            nbt.setTag(NBT_DEnch, t);
            return null;
        }

        public static DEnch readFromNBT(NBTTagCompound compound) {
            if (compound.hasKey(NBT_DEnch)) {
                NBTTagCompound t = compound.getCompoundTag(NBT_DEnch);
                return new DEnch(t.getByte("efficiency"), t.getByte("unbreaking"), t.getByte("fortune"), t.getBoolean("silktouch"));
            } else {
                return defaultEnch;
            }
        }
    }
}
