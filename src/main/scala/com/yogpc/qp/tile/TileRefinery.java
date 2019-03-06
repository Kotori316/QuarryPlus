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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import com.yogpc.qp.Config;
import com.yogpc.qp.PowerManager;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.item.ItemQuarryDebug;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.packet.distiller.AnimationMessage;
import com.yogpc.qp.utils.INBTWritable;
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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Symbol;

import static jp.t2v.lab.syntax.MapStreamSyntax.always_false;

/**
 * See {@link buildcraft.factory.tile.TileDistiller_BC8}, {@link buildcraft.api.recipes.IRefineryRecipeManager}, {@link buildcraft.energy.BCEnergyRecipes}
 */
public class TileRefinery extends APowerTile implements IEnchantableTile {
    public static final Symbol SYMBOL = Symbol.apply("RefineryPlus");
    public final DistillerTank horizontalsTank = new DistillerTank("horizontalsTank");
    public final DistillerTank upTank = new DistillerTank("upTank");
    public final DistillerTank downTank = new DistillerTank("downTank");
    private final List<DistillerTank> tanks = Arrays.asList(horizontalsTank, upTank, downTank);
    private final IFluidHandler fluidHandler = (IDummyFluidHandler) () -> {
        IFluidTankProperties[] array = tanks.stream().flatMap(distillerTank -> Stream.of(distillerTank.getTankProperties())).toArray(IFluidTankProperties[]::new);
        return array.length == 0 ? new IFluidTankProperties[]{new FluidTankProperties(null, horizontalsTank.getCapacity(), false, false)} : array;
    };

    public long rem_energy;
    @Nullable
    public FluidStack cacheIn;
    @Nullable
    public FluidStack cachedGas;
    @Nullable
    public FluidStack cachedLiquid;
    public long cacheEnergy;

    private DEnch ench = DEnch.defaultEnch;
    public float animationSpeed = 1;
    private int animationStage = 0;

    public TileRefinery() {
        horizontalsTank.setCanDrain(false);
        if (!machineDisabled) {
            horizontalsTank.predicate = fluidStack -> BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(fluidStack) != null;
        } else {
            horizontalsTank.predicate = always_false();
        }
        upTank.setCanFill(false);
        downTank.setCanFill(false);
        bcLoaded = Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_factory_modID);
    }

    @Override
    protected Symbol getSymbol() {
        return SYMBOL;
    }

    @Override
    public void G_ReInit() {
        PowerManager.configureRefinery(this, ench.efficiency, ench.unbreaking);
        tanks.forEach(distillerTank -> distillerTank.setCapacity(ench.getCapacity()));
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.ench = DEnch.readFromNBT(nbt);
        horizontalsTank.readFromNBT(nbt);
        upTank.readFromNBT(nbt);
        downTank.readFromNBT(nbt);
        updateRecipe();
        this.rem_energy = nbt.getLong("rem_energy");
        this.animationSpeed = nbt.getFloat("animationSpeed");
        this.animationStage = nbt.getInteger("animationStage");
        PowerManager.configureRefinery(this, ench.efficiency, ench.unbreaking);
        tanks.forEach(distillerTank -> distillerTank.setCapacity(ench.getCapacity()));
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        ench.writeToNBT(nbt);
        horizontalsTank.writeToNBT(nbt);
        upTank.writeToNBT(nbt);
        downTank.writeToNBT(nbt);
        nbt.setLong("rem_energy", this.rem_energy);
        nbt.setFloat("animationSpeed", this.animationSpeed);
        nbt.setInteger("animationStage", this.animationStage);
        return super.writeToNBT(nbt);
    }

    @Override
    public void update() {
        super.update();
        if (!bcLoaded || machineDisabled) return;
        if (getWorld().isRemote) {
            simpleAnimationIterate();
            return;
        }
        if (getWorld().getTotalWorldTime() % 20 == 7)
            PacketHandler.sendToAround(TileMessage.create(this), getWorld(), getPos());
        if (this.cachedGas == null || cachedLiquid == null) {
            decreaseAnimation();
            return;
        }
        double v = MjReceiver.getMJFromMicro(cacheEnergy);
        if (cacheIn == null || (!Config.content().noEnergy() && getStoredEnergy() < v) ||
            !PowerManager.useEnergyRefinery(this, v, ench.unbreaking, ench.efficiency)) {
            decreaseAnimation();
        } else {
            increaseAnimation();
            FluidStack inStack = horizontalsTank.drainInternal(cacheIn.amount, false);
            int gas = upTank.fillInternal(cachedGas, false);
            int liquid = downTank.fillInternal(cachedLiquid, false);

            if (inStack != null && inStack.amount > 0 && gas > 0 && liquid > 0) {
                horizontalsTank.drainInternal(cacheIn.amount, true);
                upTank.fillInternal(cachedGas, true);
                downTank.fillInternal(cachedLiquid, true);
                this.cacheIn = null;
                this.cachedGas = null;
                this.cachedLiquid = null;
                this.cacheEnergy = 0L;
                updateRecipe();
            }
            decreaseAnimation();
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
                cachedLiquid = recipe.outLiquid();
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
        PacketHandler.sendToAround(AnimationMessage.create(this), getWorld(), getPos());
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
    public Map<Integer, Integer> getEnchantments() {
        final Map<Integer, Integer> ret = new HashMap<>();
        if (ench.efficiency > 0)
            ret.put(EfficiencyID, (int) ench.efficiency);
        if (ench.fortune > 0)
            ret.put(FortuneID, (int) ench.fortune);
        if (ench.unbreaking > 0)
            ret.put(UnbreakingID, (int) ench.unbreaking);
        if (ench.silktouch)
            ret.put(SilktouchID, 1);
        return ret;
    }

    @Override
    public void setEnchantment(final short id, final short val) {
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

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add(getClass().getName());
        left.add(ItemQuarryDebug.tilePosToString(this).getUnformattedComponentText());
        left.add(ItemQuarryDebug.energyToString(this).getUnformattedComponentText());
        if (cacheIn != null) left.add("InputRecipe : " + cacheIn.getFluid().getName());
        if (cachedGas != null) left.add("OutGas : " + cachedGas.getFluid().getName());
        if (cachedLiquid != null) left.add("OutLiquid : " + cachedLiquid.getFluid().getName());
    }

    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        right.add("AnimationStage : " + animationStage);
        right.add("AnimationSpeed : " + animationSpeed);
    }

    @SideOnly(Side.CLIENT)
    public Runnable receiveMessage(int stage, float speed) {
        return () -> {
            animationStage = stage;
            animationSpeed = speed;
        };
    }

    public class DistillerTank extends FluidTank {
        private final String name;
        private Predicate<FluidStack> predicate = always_false();

        public DistillerTank(String name) {
            super(4 * Fluid.BUCKET_VOLUME);
            this.name = name;
        }

        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            int i = super.fillInternal(resource, doFill);
            if (doFill) updateRecipe();
            return i;
        }

        @Nullable
        @Override
        public FluidStack drainInternal(int maxDrain, boolean doDrain) {
            FluidStack stack = super.drainInternal(maxDrain, doDrain);
            if (doDrain) updateRecipe();
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

        @Override
        public String toString() {
            return name + " " + Optional.ofNullable(fluid).map(s -> s.getFluid().getName() + " : " + s.amount).orElse("None");
        }

        public double getAA(double d) {
            if (fluid == null) {
                return 0;
            }
            int i = fluid.getFluid().isGaseous() ? -1 : 1;
            return (double) getFluidAmount() / (double) getCapacity() * d * i;
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

        DEnch(int efficiency, int unbreaking, int fortune, boolean silktouch) {
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

        @Override
        public String toString() {
            return String.format("DEnch %d, %d, %d, %s", efficiency, unbreaking, fortune, silktouch);
        }
    }
}
