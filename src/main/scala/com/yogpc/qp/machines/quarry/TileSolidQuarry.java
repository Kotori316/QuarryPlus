package com.yogpc.qp.machines.quarry;

import java.util.List;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import scala.Symbol;

public class TileSolidQuarry extends TileQuarry implements IInteractionObject {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.solidquarry;
    private static final long fuelEfficiency = 4 * APowerTile.MicroJtoMJ; // 40 RF/t
    private ItemStack fuel = ItemStack.EMPTY;
    private int fuelCount = 0;

    public TileSolidQuarry() {
        super(Holder.solidQuarryType());
    }

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
            int burn = TileEntityFurnace.getBurnTimes().getOrDefault(fuel.getItem(), 0) / 5;
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
    public void read(NBTTagCompound nbt) {
        super.read(nbt);
        fuelCount = nbt.getInt("fuelCount");
        fuel = ItemStack.read(nbt.getCompound("fuel"));
    }

    @Override
    public NBTTagCompound write(NBTTagCompound nbt) {
        nbt.setInt("fuelCount", fuelCount);
        nbt.setTag("fuel", fuel.serializeNBT());
        return super.write(nbt);
    }

    @Override
    public Symbol getSymbol() {
        return BlockSolidQuarry.SYMBOL;
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.solidquarry;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(getPos()) == this;
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
            fuel = ItemStack.EMPTY;
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
            if (fuel.getCount() <= count)
                return removeStackFromSlot(0);
            else
                return fuel.split(count);
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
        fuel = ItemStack.EMPTY;
        super.clear();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && fuel.isEmpty();
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
        return super.isWorking() && (fuelCount > 0 || !fuel.isEmpty());
    }

    @Override
    public List<ITextComponent> getDebugMessages() {
        List<ITextComponent> list = super.getDebugMessages();
        // I know super.getDebugMessages returns ArrayList.
        list.add(new TextComponentString("FuelCount : " + fuelCount));
        return list;
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerSolidQuarry(this, playerIn);
    }

    @Override
    public String getGuiID() {
        return GUI_ID;
    }
}
