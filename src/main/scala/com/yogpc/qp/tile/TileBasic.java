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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cofh.api.tileentity.IInventoryConnection;
import com.yogpc.qp.BlockData;
import com.yogpc.qp.PowerManager;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.ReflectionHelper;
import com.yogpc.qp.compat.InvUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

@Optional.Interface(iface = "cofh.api.tileentity.IInventoryConnection", modid = QuarryPlus.Optionals.COFH_tileentity)
public abstract class TileBasic extends APowerTile implements IEnchantableTile, IInventory, IInventoryConnection {
    protected EnumFacing pump = null;

    public final List<BlockData> fortuneList = new ArrayList<>();
    public final List<BlockData> silktouchList = new ArrayList<>();
    public boolean fortuneInclude, silktouchInclude;

    protected byte unbreaking;
    protected byte fortune;
    protected boolean silktouch;
    protected byte efficiency;

    protected final LinkedList<ItemStack> cacheItems = new LinkedList<>();
    protected IItemHandler handler = new InvWrapper(this) {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return ItemStack.EMPTY;
        }
    };

    public abstract void G_renew_powerConfigure();

    protected abstract void G_destroy();

    @Override
    public final void onChunkUnload() {
        G_destroy();
        super.onChunkUnload();
    }

    protected void S_pollItems() {
        ItemStack is;
        while (null != (is = this.cacheItems.poll())) {
            ItemStack stack = InvUtils.injectToNearTile(getWorld(), getPos(), is);
            if (stack.getCount() > 0) {
                this.cacheItems.add(stack);
                break;
            }
        }
    }

    protected boolean S_breakBlock(final int x, final int y, final int z) {
        final List<ItemStack> dropped = new LinkedList<>();
        //noinspection ConstantConditions
        final IBlockState b = getWorld().getChunkProvider().getLoadedChunk(x >> 4, z >> 4).getBlockState(x & 0xF, y, z & 0xF);
        BlockPos pos = new BlockPos(x, y, z);
        if (b.getBlock().isAir(b, getWorld(), pos))
            return true;
        if (pump != null && TilePump.isLiquid(b, false, getWorld(), pos)) {
            final TileEntity te = getWorld().getTileEntity(getPos().offset(pump));
            if (!(te instanceof TilePump)) {
                this.pump = null;
                G_renew_powerConfigure();
                return true;
            }
            return ((TilePump) te).S_removeLiquids(this, x, y, z);
        }
        if (!PowerManager.useEnergyBreak(this, b.getBlockHardness(getWorld(), pos),
                S_addDroppedItems(dropped, b.getBlock(), x, y, z), this.unbreaking))
            return false;
        this.cacheItems.addAll(dropped);
        this.getWorld().destroyBlock(pos, false);
        return true;
    }

    boolean S_connect(final EnumFacing fd) {
        if (pump != null) {
            final TileEntity te = this.getWorld().getTileEntity(getPos().offset(pump));
            if (te instanceof TilePump && this.pump != fd)
                return false;
        }
        this.pump = fd;
        G_renew_powerConfigure();
        return true;
    }

    private byte S_addDroppedItems(final Collection<ItemStack> list, final Block b, final int x, final int y, final int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = getWorld().getBlockState(pos);
        if (b.canSilkHarvest(this.getWorld(), pos, state, null)
                && this.silktouch
                && this.silktouchList.contains(new BlockData(ForgeRegistries.BLOCKS.getKey(b), state.getBlock().getMetaFromState(state))) == this.silktouchInclude) {
            list.add((ItemStack) ReflectionHelper.invoke(createStackedBlock, b, state));
            return -1;
        }
        if (this.fortuneList.contains(new BlockData(ForgeRegistries.BLOCKS.getKey(b),
                state.getBlock().getMetaFromState(state))) == this.fortuneInclude) {
            list.addAll(b.getDrops(getWorld(), pos, state, this.fortune));
            return this.fortune;
        }
        list.addAll(b.getDrops(getWorld(), pos, state, 0));
        return 0;
    }

    public static final Method createStackedBlock = ReflectionHelper.getMethod(Block.class,
            new String[]{"func_149644_j", "getSilkTouchDrop"}, new Class<?>[]{IBlockState.class});

    @Override
    public void readFromNBT(final NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        this.silktouch = nbttc.getBoolean("silktouch");
        this.fortune = nbttc.getByte("fortune");
        this.efficiency = nbttc.getByte("efficiency");
        this.unbreaking = nbttc.getByte("unbreaking");
        this.fortuneInclude = nbttc.getBoolean("fortuneInclude");
        this.silktouchInclude = nbttc.getBoolean("silktouchInclude");
        readLongCollection(nbttc.getTagList("fortuneList", 10), this.fortuneList);
        readLongCollection(nbttc.getTagList("silktouchList", 10), this.silktouchList);
    }

    private static void readLongCollection(final NBTTagList nbttl, final Collection<BlockData> target) {
        target.clear();
        for (int i = 0; i < nbttl.tagCount(); i++) {
            final NBTTagCompound c = nbttl.getCompoundTagAt(i);
            target.add(new BlockData(c.getString("name"), c.getInteger("meta")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbttc) {
        nbttc.setBoolean("silktouch", this.silktouch);
        nbttc.setByte("fortune", this.fortune);
        nbttc.setByte("efficiency", this.efficiency);
        nbttc.setByte("unbreaking", this.unbreaking);
        nbttc.setBoolean("fortuneInclude", this.fortuneInclude);
        nbttc.setBoolean("silktouchInclude", this.silktouchInclude);
        nbttc.setTag("fortuneList", writeLongCollection(this.fortuneList));
        nbttc.setTag("silktouchList", writeLongCollection(this.silktouchList));
        return super.writeToNBT(nbttc);
    }

    private static NBTTagList writeLongCollection(final Collection<BlockData> target) {
        final NBTTagList nbttl = new NBTTagList();
        for (final BlockData l : target) {
            final NBTTagCompound c = new NBTTagCompound();
            c.setString("name", l.name.toString());
            c.setInteger("meta", l.meta);
            nbttl.appendTag(c);
        }
        return nbttl;
    }

    @Override
    public Map<Integer, Byte> getEnchantments() {
        final Map<Integer, Byte> ret = new HashMap<>();
        if (this.efficiency > 0)
            ret.put(EfficiencyID, this.efficiency);
        if (this.fortune > 0)
            ret.put(FortuneID, this.fortune);
        if (this.unbreaking > 0)
            ret.put(UnbreakingID, this.unbreaking);
        if (this.silktouch)
            ret.put(SilktouchID, (byte) 1);
        return ret;
    }

    @Override
    public void setEnchantent(final short id, final short val) {
        if (id == EfficiencyID)
            this.efficiency = (byte) val;
        else if (id == FortuneID)
            this.fortune = (byte) val;
        else if (id == UnbreakingID)
            this.unbreaking = (byte) val;
        else if (id == SilktouchID && val > 0)
            this.silktouch = true;
    }

    @Override
    public int getSizeInventory() {
        return Math.max(1, this.cacheItems.size());
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(final int i) {
        return i < 0 || i >= this.cacheItems.size() ? ItemStack.EMPTY : this.cacheItems.get(i);
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(final int i, final int a) {
        if (i < 0 || i >= this.cacheItems.size())
            return ItemStack.EMPTY;
        final ItemStack from = this.cacheItems.get(i);
        final ItemStack res = new ItemStack(from.getItem(), Math.min(a, from.getCount()), from.getItemDamage());
        if (from.hasTagCompound())
            //noinspection ConstantConditions
            res.setTagCompound(from.getTagCompound().copy());
        from.shrink(res.getCount());
        if (from.isEmpty())
            this.cacheItems.remove(i);
        return res;
    }


    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int i) {
        return i < 0 || i >= this.cacheItems.size() ? ItemStack.EMPTY : this.cacheItems.get(i);
    }

    @Override
    public void setInventorySlotContents(final int i, final ItemStack is) {
        if (!is.isEmpty())
            System.err.println("QuarryPlus WARN: call setInventorySlotContents with non null ItemStack.");
        if (i >= 0 && i < this.cacheItems.size())
            this.cacheItems.remove(i);
    }

    @Override
    public String getName() {
        return "container.yog.basic";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return cacheItems.isEmpty();
    }

    @Override
    public void clear() {
        cacheItems.clear();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_tileentity)
    public ConnectionType canConnectInventory(final EnumFacing arg0) {
        return ConnectionType.FORCE;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
        } else {
            return super.getCapability(capability, facing);
        }
    }
}
