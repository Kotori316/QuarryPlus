package com.yogpc.qp.machines.quarry;

import java.util.List;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class TileSolidQuarry extends TileQuarry implements INamedContainerProvider {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.solidquarry;
    private static final long fuelEfficiency = 4 * APowerTile.MJToMicroMJ; // 40 RF/t
    private ItemStack fuel = ItemStack.EMPTY;
    private int fuelCount = 0;
    public final IIntArray fuelCountAccessor;

    public TileSolidQuarry() {
        super(Holder.solidQuarryType());
        fuelCountAccessor = new IIntArray() {
            @Override
            public int get(int index) {
                return fuelCount;
            }

            @Override
            public void set(int index, int value) {
                fuelCount = value;
            }

            @Override
            public int size() {
                return 1;
            }
        };
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    protected void getEnergyInTick() {
        if (fuelCount > 0) {
            if (getEnergy(fuelEfficiency, false) == fuelEfficiency) {
                fuelCount -= 1;
                getEnergy(fuelEfficiency, true);
            }
        } else {
            int burn = fuel.getBurnTime();
            burn = ForgeEventFactory.getItemBurnTime(fuel, burn == -1 ? FurnaceTileEntity.getBurnTimes().getOrDefault(fuel.getItem(), 0) : burn);
            // int burn = ForgeHooks.getBurnTime(fuel);
            if (burn > 0) {
                fuelCount += burn / 5;
                if (fuel.hasContainerItem() && fuel.getCount() == 1) {
                    setInventorySlotContents(0, fuel.getContainerItem());
                } else {
                    decrStackSize(0, 1);
                }
            }
        }
    }

    @Override
    protected BlockState S_getFillBlock() {
        return Blocks.AIR.getDefaultState(); // Replace with dummy block is not allowed.
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);
        fuelCount = nbt.getInt("fuelCount");
        fuel = ItemStack.read(nbt.getCompound("fuel"));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("fuelCount", fuelCount);
        nbt.put("fuel", fuel.serializeNBT());
        return super.write(nbt);
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.solidquarry;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return player.world.getTileEntity(getPos()) == this;
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
        return index == 0 && FurnaceTileEntity.isFuel(stack);
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
                if (slot == 0) {
                    ItemStack stackInSlot = getInv().getStackInSlot(slot);
                    if (stackInSlot.getItem() == Items.BUCKET) {
                        if (simulate) {
                            if (stackInSlot.getCount() < amount) {
                                return stackInSlot.copy();
                            } else {
                                ItemStack copy = stackInSlot.copy();
                                copy.setCount(amount);
                                return copy;
                            }
                        } else {
                            int m = Math.min(stackInSlot.getCount(), amount);
                            ItemStack decrStackSize = getInv().decrStackSize(slot, m);
                            getInv().markDirty();
                            return decrStackSize;
                        }
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
                return super.extractItem(slot, amount, simulate);
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
        list.add(new StringTextComponent("FuelCount : " + fuelCount));
        return list;
    }

    @Override
    public Container createMenu(int id, PlayerInventory i, PlayerEntity p) {
        return new ContainerSolidQuarry(id, p, pos);
    }
}
