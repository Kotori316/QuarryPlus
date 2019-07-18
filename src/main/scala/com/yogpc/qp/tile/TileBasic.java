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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cofh.api.tileentity.IInventoryConnection;
import com.yogpc.qp.Config;
import com.yogpc.qp.PowerManager;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.utils.BlockData;
import com.yogpc.qp.utils.NBTBuilder;
import com.yogpc.qp.utils.NoDuplicateList;
import com.yogpc.qp.utils.ReflectionHelper;
import com.yogpc.qp.version.VersionUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import static com.yogpc.qp.tile.IAttachment.Attachments.ALL;
import static com.yogpc.qp.tile.IAttachment.Attachments.EXP_PUMP;
import static com.yogpc.qp.tile.IAttachment.Attachments.FLUID_PUMP;
import static jp.t2v.lab.syntax.MapStreamSyntax.always_true;
import static jp.t2v.lab.syntax.MapStreamSyntax.not;
import static jp.t2v.lab.syntax.MapStreamSyntax.toAny;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;

@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.tileentity.IInventoryConnection", modid = QuarryPlus.Optionals.COFH_modID)
public abstract class TileBasic extends APowerTile implements IEnchantableTile, HasInv, IInventoryConnection, IAttachable {

    private static final Set<IAttachment.Attachments<?>> VALID_ATTACHMENTS = ALL;
    protected final Map<IAttachment.Attachments<?>, EnumFacing> facingMap = new HashMap<>();

    public NoDuplicateList<BlockData> fortuneList = NoDuplicateList.create(ArrayList::new);
    public NoDuplicateList<BlockData> silktouchList = NoDuplicateList.create(ArrayList::new);
    public boolean fortuneInclude, silktouchInclude;

    protected byte unbreaking;
    protected byte fortune;
    protected boolean silktouch;
    protected byte efficiency;

    protected final LinkedList<ItemStack> cacheItems = new LinkedList<>();
    protected final IItemHandler handler = createHandler();

    protected Map<Integer, Integer> ench = new HashMap<>();

    public List<IModule> modules = Collections.emptyList();

    /**
     * Where quarry stops its work. Dig blocks at this value.
     */
    public int yLevel = 1;

    /**
     * Reconfigure energy capacity and amount to receive.
     */
    public abstract void G_renew_powerConfigure();

    /**
     * Called when the work was finished and this block was removed or unloaded.
     */
    protected abstract void G_destroy();

    @Override
    public final void onChunkUnload() {
        G_destroy();
        super.onChunkUnload();
    }

    /**
     * Insert as much items as possible to near inventory .
     */
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

    /**
     * Dig block and collect its drops.
     *
     * @param replace to which the block will be replaced.
     * @return true if you should get next target, false if you should try to break again.
     */
    protected boolean S_breakBlock(final int x, final int y, final int z, IBlockState replace) {
        final List<ItemStack> dropped = new ArrayList<>(2);
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

        BI bi = S_addDroppedItems(dropped, blockState, pos);
        if (!PowerManager.useEnergyBreak(this, blockState.getBlockHardness(getWorld(), pos), bi.b, this.unbreaking, bi.b1))
            return false;
        modules.forEach(iModule -> iModule.invoke(new IModule.BeforeBreak(bi.i, world, pos)));
        this.cacheItems.addAll(dropped);

        if (facingMap.containsKey(FLUID_PUMP) && TilePump.isLiquid(blockState)) {
            final TileEntity te = world.getTileEntity(getPos().offset(facingMap.get(FLUID_PUMP)));
            if (!(te instanceof TilePump)) {
                facingMap.remove(FLUID_PUMP);
                G_renew_powerConfigure();
                return true;
            }
            boolean b = ((TilePump) te).S_removeLiquids(this, x, y, z);
            if (blockState.getMaterial().isLiquid()) return b;
            // fluid should be replaced with air.
            // other blocks will be replaced with block.
        }
        // Replace block
        getWorld().playEvent(2001, pos, Block.getStateId(blockState));
        getWorld().setBlockState(pos, replace, 3);
        return true;
    }

    @Override
    public boolean connectAttachment(final EnumFacing facing, IAttachment.Attachments<? extends APacketTile> attachments, boolean simulate) {
        if (facingMap.containsKey(attachments)) {
            TileEntity entity = getWorld().getTileEntity(getPos().offset(facingMap.get(attachments)));
            if (attachments.test(entity) && facingMap.get(attachments) != facing) {
                return false;
            }
        }
        if (!simulate) {
            facingMap.put(attachments, facing);
            G_renew_powerConfigure();
            modules = facingMap.entrySet().stream()
                .map(values(pos::offset))
                .map(values(world::getTileEntity))
                .map(toAny(IAttachment.Attachments::module))
                .flatMap(iModule -> iModule.map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
        }
        return true;
    }

    @Override
    public boolean isValidAttachment(IAttachment.Attachments<? extends APacketTile> attachments) {
        return VALID_ATTACHMENTS.contains(attachments);
    }

    public void setYLevel(int yLevel) {
        this.yLevel = yLevel;
        if (yLevel <= 0) {
            if (Config.content().debug()) {
                QuarryPlus.LOGGER.warn("Quarry yLevel is set to " + yLevel + ".");
            }
        }
    }

    private BI S_addDroppedItems(final Collection<ItemStack> collection, final IBlockState state, final BlockPos pos) {
        Block block = state.getBlock();
        byte i;
        int xp = 0;
        QuarryFakePlayer fakePlayer = QuarryFakePlayer.get(((WorldServer) getWorld()));
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe());
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            Set<ItemStack> rawItems;
            if (block.canSilkHarvest(getWorld(), pos, state, fakePlayer) && this.silktouch
                && silktouchList.contains(new BlockData(block, state)) == this.silktouchInclude) {
                List<ItemStack> list = new ArrayList<>(1);
                list.add((ItemStack) ReflectionHelper.invoke(createStackedBlock, block, state));
                rawItems = new HashSet<>(list);
                ForgeEventFactory.fireBlockHarvesting(list, world, pos, state, 0, 1.0f, true, fakePlayer);
                collection.addAll(list);
                i = -1;
            } else {
                boolean b = fortuneList.contains(new BlockData(block, state)) == this.fortuneInclude;
                byte fortuneLevel = b ? this.fortune : 0;
                NonNullList<ItemStack> list = NonNullList.create();
                getDrops(getWorld(), pos, state, block, fortuneLevel, list);
//                if (list.isEmpty() && Config.content().debug())
//                    ReflectionHelper.checkGetDrops(getWorld(), pos, state, block, fortuneLevel, list);
                rawItems = new HashSet<>(list);
                ForgeEventFactory.fireBlockHarvesting(list, world, pos, state, fortuneLevel, 1.0f, false, fakePlayer);
//                if (!rawItems.isEmpty() && list.isEmpty() && Config.content().debug())
//                    QuarryPlus.LOGGER.info("Drop items were removed during BlockHarvestingEvent.");
                collection.addAll(list);
                i = fortuneLevel;
            }
            if (facingMap.containsKey(EXP_PUMP)) {
                xp += event.getExpToDrop();
                if (InvUtils.hasSmelting(fakePlayer.getHeldItemMainhand())) {
                    xp += getSmeltingXp(collection, rawItems);
                }
            }
        } else {
            i = -2;
        }
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, VersionUtil.empty());
        return new BI(i, xp, facingMap.containsKey(IAttachment.Attachments.REPLACER));
    }

    /**
     * Helper method for Thaumcraft.
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public static void getDrops(World world, BlockPos pos, IBlockState state, Block block, int fortuneLevel, NonNullList<ItemStack> list) {
        if (QuarryPlus.Optionals.Thaumcraft_modID.equals(block.getRegistryName().getResourceDomain())) {
            list.addAll(block.getDrops(world, pos, state, fortuneLevel));
        } else {
            block.getDrops(list, world, pos, state, fortuneLevel);
        }
    }

    /**
     * @param stacks read only
     * @param raw    read only
     * @return The amount of xp by smelting items.
     */
    public static int getSmeltingXp(Collection<ItemStack> stacks, Collection<ItemStack> raw) {
        return stacks.stream().filter(not(raw::contains)).mapToInt(stack ->
            floorFloat(FurnaceRecipes.instance().getSmeltingExperience(stack) * VersionUtil.getCount(stack))).sum();
    }

    static int floorFloat(float value) {
        int i = MathHelper.floor(value);
        return i + (Math.random() < (value - i) ? 1 : 0);
    }

    public static final Method createStackedBlock = ReflectionHelper.getMethod(Block.class,
        new String[]{"func_180643_i", "getSilkTouchDrop"}, new Class<?>[]{IBlockState.class});

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.silktouch = nbt.getBoolean("silktouch");
        this.fortune = nbt.getByte("fortune");
        this.efficiency = nbt.getByte("efficiency");
        this.unbreaking = nbt.getByte("unbreaking");
        this.fortuneInclude = nbt.getBoolean("fortuneInclude");
        this.silktouchInclude = nbt.getBoolean("silktouchInclude");
        this.yLevel = Math.max(nbt.getInteger("yLevel"), 1);
        readLongCollection(nbt.getTagList("fortuneList", 10), this.fortuneList);
        readLongCollection(nbt.getTagList("silktouchList", 10), this.silktouchList);
        ench = NBTBuilder.fromList(nbt.getTagList("enchList", Constants.NBT.TAG_COMPOUND), n -> n.getInteger("id"), n -> n.getInteger("value"),
            s -> Enchantment.getEnchantmentByID(s) != null, always_true());
    }

    private static void readLongCollection(final NBTTagList list, final Collection<BlockData> target) {
        target.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound c = list.getCompoundTagAt(i);
            target.add(new BlockData(c.getString(BlockData.Name_NBT()), c.getInteger(BlockData.Meta_NBT())));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        nbt.setBoolean("silktouch", this.silktouch);
        nbt.setByte("fortune", this.fortune);
        nbt.setByte("efficiency", this.efficiency);
        nbt.setByte("unbreaking", this.unbreaking);
        nbt.setBoolean("fortuneInclude", this.fortuneInclude);
        nbt.setBoolean("silktouchInclude", this.silktouchInclude);
        nbt.setTag("fortuneList", writeLongCollection(this.fortuneList));
        nbt.setTag("silktouchList", writeLongCollection(this.silktouchList));
        nbt.setInteger("yLevel", this.yLevel);
        Function<Integer, Short> function = Integer::shortValue;
        nbt.setTag("enchList", NBTBuilder.fromMap(ench, "id", "value", function.andThen(NBTTagShort::new), function.andThen(NBTTagShort::new)));
        return super.writeToNBT(nbt);
    }

    private static NBTTagList writeLongCollection(final Collection<BlockData> target) {
        return target.stream().map(l -> l.toNBT().getCompoundTag(BlockData.BlockData_NBT())).collect(VersionUtil.toNBTList());
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Map<Integer, Integer> getEnchantments() {
        final Map<Integer, Integer> ret = new HashMap<>(ench);
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
    @SuppressWarnings("Duplicates")
    public void setEnchantment(final short id, final short val) {
        if (id == EfficiencyID)
            this.efficiency = (byte) val;
        else if (id == FortuneID)
            this.fortune = (byte) val;
        else if (id == UnbreakingID)
            this.unbreaking = (byte) val;
        else if (id == SilktouchID)
            this.silktouch = val > 0;

        if (val > 0) {
            ench.put((int) id, (int) val);
        }
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
        Optional.ofNullable(from.getTagCompound()).map(NBTTagCompound::copy).ifPresent(res::setTagCompound);
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
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
    public final ConnectionType canConnectInventory(EnumFacing from) {
        return ConnectionType.FORCE;
    }

    private static class BI {
        final byte b;
        final int i;
        final boolean b1;

        private BI(byte b, int i, boolean b1) {
            this.b = b;
            this.i = i;
            this.b1 = b1;
        }
    }
}
