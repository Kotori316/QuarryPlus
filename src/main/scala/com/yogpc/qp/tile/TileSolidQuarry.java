package com.yogpc.qp.tile;

import java.util.List;

import com.yogpc.qp.block.BlockSolidQuarry;
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.version.VersionUtil;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import scala.Symbol;

public class TileSolidQuarry extends TileQuarry {
    private static final long fuelEfficiency = 4 * APowerTile.MicroJtoMJ; // 40 RF/t
    private ItemStack fuel = VersionUtil.empty();
    private int fuelCount = 0;

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    protected void S_updateEntity() {
        if (machineDisabled) return;
        if (fuelCount > 0) {
            fuelCount -= 1;
            getEnergy(fuelEfficiency, true);
        } else {
            int burn = TileEntityFurnace.getItemBurnTime(fuel) / 5;
            if (burn > 0) {
                fuelCount += burn;
                decrStackSize(0, 1);
            }
        }
        super.S_updateEntity();
    }

    @Override
    protected IBlockState S_getFillBlock() {
        return Blocks.AIR.getDefaultState(); // Replace with dummy block is not allowed.
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        fuelCount = nbt.getInteger("fuelCount");
        fuel = VersionUtil.fromNBTTag(nbt.getCompoundTag("fuel"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("fuelCount", fuelCount);
        nbt.setTag("fuel", fuel.serializeNBT());
        return super.writeToNBT(nbt);
    }

    @Override
    protected Symbol getSymbol() {
        return BlockSolidQuarry.SYMBOL;
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.solidquarry;
    }

    @Override
    public String getName() {
        return TranslationKeys.solidquarry;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return getWorld().getTileEntity(getPos()) == this;
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        if (i == 0) return fuel;
        else return super.getStackInSlot(i - 1);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index == 0) {
            ItemStack temp = this.fuel;
            fuel = VersionUtil.empty();
            return temp;
        }
        return super.removeStackFromSlot(index - 1);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == 0) fuel = stack;
        else super.setInventorySlotContents(index - 1, stack);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == 0) {
            if (VersionUtil.getCount(fuel) <= count)
                return removeStackFromSlot(0);
            else
                return fuel.splitStack(count);
        }
        return super.decrStackSize(index - 1, count);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0 && TileEntityFurnace.isItemFuel(stack);
    }

    @Override
    public void clear() {
        fuel = VersionUtil.empty();
        super.clear();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && VersionUtil.isEmpty(fuel);
    }

    @Override
    public IItemHandlerModifiable createHandler() {
        return new InvWrapper(this) {
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return slot == 0 ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
            }
        };
    }

    @Override
    protected boolean isWorking() {
        return super.isWorking() && (fuelCount > 0 || VersionUtil.nonEmpty(fuel));
    }

    @Override
    public List<ITextComponent> getDebugMessages() {
        List<ITextComponent> list = super.getDebugMessages();
        // I know super.getDebugMessages returns ArrayList.
        list.add(new TextComponentString("FuelCount : " + fuelCount));
        return list;
    }
}
