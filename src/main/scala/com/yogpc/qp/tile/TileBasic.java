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
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.version.VersionUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.tileentity.IInventoryConnection", modid = QuarryPlus.Optionals.COFH_tileentity)
public abstract class TileBasic extends APowerTile implements IEnchantableTile, HasInv, IInventoryConnection {
    @Nullable
    protected EnumFacing pump = null;

    public final NoDuplicateList<BlockData> fortuneList = NoDuplicateList.create(ArrayList::new);
    public final NoDuplicateList<BlockData> silktouchList = NoDuplicateList.create(ArrayList::new);
    public boolean fortuneInclude, silktouchInclude;

    protected byte unbreaking;
    protected byte fortune;
    protected boolean silktouch;
    protected byte efficiency;

    protected final LinkedList<ItemStack> cacheItems = new LinkedList<>();
    protected final IItemHandler handler = new InvWrapper(this) {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
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
            if (VersionUtil.getCount(stack) > 0) {
                this.cacheItems.add(stack);
                break;
            }
        }
    }

    protected boolean S_breakBlock(final int x, final int y, final int z) {
        final List<ItemStack> dropped = new LinkedList<>();
        Chunk loadedChunk = getWorld().getChunkProvider().getLoadedChunk(x >> 4, z >> 4);
        final IBlockState blockState;
        BlockPos pos = new BlockPos(x, y, z);
        if (loadedChunk == null) {
            blockState = getWorld().getBlockState(pos);
        } else {
            blockState = loadedChunk.getBlockState(x & 0xF, y, z & 0xF);
        }
        if (blockState.getBlock().isAir(blockState, getWorld(), pos))
            return true;
        if (pump != null && TilePump.isLiquid(blockState)) {
            final TileEntity te = getWorld().getTileEntity(getPos().offset(pump));
            if (!(te instanceof TilePump)) {
                this.pump = null;
                G_renew_powerConfigure();
                return true;
            }
            return ((TilePump) te).S_removeLiquids(this, x, y, z);
        }
        if (!PowerManager.useEnergyBreak(this, blockState.getBlockHardness(getWorld(), pos),
            S_addDroppedItems(dropped, blockState, pos), this.unbreaking))
            return false;
        this.cacheItems.addAll(dropped);
        this.getWorld().destroyBlock(pos, false);
        return true;
    }

    boolean S_connect(final EnumFacing facing) {
        if (pump != null) {
            final TileEntity te = this.getWorld().getTileEntity(getPos().offset(pump));
            if (te instanceof TilePump && this.pump != facing)
                return false;
        }
        this.pump = facing;
        G_renew_powerConfigure();
        return true;
    }

    private byte S_addDroppedItems(final Collection<ItemStack> collection, final IBlockState state, final BlockPos pos) {
        Block block = state.getBlock();
        byte i;
        QuarryFakePlayer fakePlayer = QuarryFakePlayer.get(((WorldServer) getWorld()));
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe());
        if (block.canSilkHarvest(getWorld(), pos, state, fakePlayer) && this.silktouch
            && silktouchList.contains(new BlockData(block, state)) == this.silktouchInclude) {
            collection.add((ItemStack) ReflectionHelper.invoke(createStackedBlock, block, state));
            i = -1;
        } else {
            boolean b = fortuneList.contains(new BlockData(block, state)) == this.fortuneInclude;
            byte fortuneLevel = b ? this.fortune : 0;
            NonNullList<ItemStack> list = NonNullList.create();
            getDrops(getWorld(), pos, state, block, fortuneLevel, list);
            collection.addAll(list);
            i = fortuneLevel;
        }
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, VersionUtil.empty());
        return i;
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public static void getDrops(World world, BlockPos pos, IBlockState state, Block block, int fortuneLevel, NonNullList<ItemStack> list) {
        if (QuarryPlus.Optionals.Thaumcraft_modID.equals(block.getRegistryName().getResourceDomain())) {
            list.addAll(block.getDrops(world, pos, state, fortuneLevel));
        } else {
            block.getDrops(list, world, pos, state, fortuneLevel);
        }
    }

    public static final Method createStackedBlock = ReflectionHelper.getMethod(Block.class,
        new String[]{"func_180643_i", "getSilkTouchDrop"}, new Class<?>[]{IBlockState.class});

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
            target.add(new BlockData(c.getString(BlockData.Name_NBT()), c.getInteger(BlockData.Meta_NBT())));
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
            nbttl.appendTag(l.writeToNBT(new NBTTagCompound()).getCompoundTag(BlockData.BlockData_NBT()));
        }
        return nbttl;
    }

    @Override
    public Map<Integer, Integer> getEnchantments() {
        final Map<Integer, Integer> ret = new HashMap<>();
        if (this.efficiency > 0)
            ret.put(EfficiencyID, (int) this.efficiency);
        if (this.fortune > 0)
            ret.put(FortuneID, (int) this.fortune);
        if (this.unbreaking > 0)
            ret.put(UnbreakingID, (int) this.unbreaking);
        if (this.silktouch)
            ret.put(SilktouchID, 1);
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
        else if (id == SilktouchID)
            this.silktouch = val > 0;
    }

    @Override
    public int getSizeInventory() {
        return Math.max(1, this.cacheItems.size());
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(final int i) {
        return i < 0 || i >= this.cacheItems.size() ? com.yogpc.qp.version.VersionUtil.empty() : this.cacheItems.get(i);
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(final int index, final int count) {
        if (index < 0 || index >= this.cacheItems.size())
            return com.yogpc.qp.version.VersionUtil.empty();
        final ItemStack from = this.cacheItems.get(index);
        final ItemStack res = new ItemStack(from.getItem(), Math.min(count, VersionUtil.getCount(from)), from.getItemDamage());
        if (from.hasTagCompound())
            //noinspection ConstantConditions
            res.setTagCompound(from.getTagCompound().copy());
        VersionUtil.shrink(from, VersionUtil.getCount(res));
        if (VersionUtil.isEmpty(from))
            this.cacheItems.remove(index);
        return res;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        return index < 0 || index >= this.cacheItems.size() ? com.yogpc.qp.version.VersionUtil.empty() : this.cacheItems.remove(index);
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        if (VersionUtil.nonEmpty(stack))
            QuarryPlus.LOGGER.warn("QuarryPlus WARN: call setInventorySlotContents with non null ItemStack.");
        removeStackFromSlot(index);
    }

    @Override
    public String getName() {
        return TranslationKeys.MACHINE_BUFFER;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return false;
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

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_tileentity)
    public ConnectionType canConnectInventory(EnumFacing from) {
        return ConnectionType.FORCE;
    }
}
