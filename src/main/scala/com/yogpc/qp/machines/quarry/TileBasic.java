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

package com.yogpc.qp.machines.quarry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasInv;
import com.yogpc.qp.machines.base.IAttachable;
import com.yogpc.qp.machines.base.IAttachment;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.machines.workbench.BlockData;
import com.yogpc.qp.utils.NBTBuilder;
import com.yogpc.qp.utils.NoDuplicateList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.crafting.VanillaRecipeTypes;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import static com.yogpc.qp.machines.base.IAttachment.Attachments.ALL;
import static com.yogpc.qp.machines.base.IAttachment.Attachments.EXP_PUMP;
import static com.yogpc.qp.machines.base.IAttachment.Attachments.FLUID_PUMP;
import static jp.t2v.lab.syntax.MapStreamSyntax.byKey;
import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.keys;
import static jp.t2v.lab.syntax.MapStreamSyntax.not;
import static jp.t2v.lab.syntax.MapStreamSyntax.toEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;

//@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.tileentity.IInventoryConnection", modid = QuarryPlus.Optionals.COFH_modID)
public abstract class TileBasic extends APowerTile implements IEnchantableTile, HasInv, IAttachable/*, IInventoryConnection*/ {

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

    protected Map<ResourceLocation, Integer> ench = new HashMap<>();

    public List<IModule> modules = new ArrayList<>();

    /**
     * Where quarry stops its work. Dig blocks at this value.
     */
    public int yLevel = 1;

    public TileBasic(TileEntityType<?> type) {
        super(type);
    }

    /**
     * Reconfigure energy capacity and amount to receive.
     */
    public abstract void G_renew_powerConfigure();

    /**
     * Called when the work was finished and this block was removed or unloaded.
     */
    protected abstract void G_destroy();

    @Override
    public final void onChunkUnloaded() {
        G_destroy();
        super.onChunkUnloaded();
    }

    /**
     * Insert as much items as possible to near inventory .
     */
    protected void S_pollItems() {
        ItemStack is;
        while (null != (is = this.cacheItems.poll())) {
            ItemStack stack = InvUtils.injectToNearTile(world, getPos(), is);
            if (stack.getCount() > 0) {
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
        final IBlockState blockState;
        BlockPos pos = new BlockPos(x, y, z);
        blockState = world.getBlockState(pos);
        if (blockState.getBlock().isAir(blockState, world, pos))
            return true;

        BI bi = S_addDroppedItems(dropped, blockState, pos);
        if (!PowerManager.useEnergyBreak(this, blockState.getBlockHardness(world, pos), bi.b, this.unbreaking, bi.b1))
            return false;
        modules.forEach(iModule -> iModule.invoke(new IModule.OnBreak(bi.i)));
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
        world.playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(blockState));
        world.setBlockState(pos, replace, 3);
        return true;
    }

    @Override
    public boolean connectAttachment(final EnumFacing facing, IAttachment.Attachments<? extends APacketTile> attachments, boolean simulate) {
        TileEntity entity = world.getTileEntity(getPos().offset(facingMap.get(attachments)));
        if (facingMap.containsKey(attachments)) {
            if (attachments.test(entity) && facingMap.get(attachments) != facing) {
                return false;
            }
        }
        if (!simulate) {
            facingMap.put(attachments, facing);
            G_renew_powerConfigure();
            attachments.module(entity).ifPresent(modules::add);
        }
        return true;
    }

    @Override
    public boolean isValidAttachment(IAttachment.Attachments<? extends APacketTile> attachments) {
        return VALID_ATTACHMENTS.contains(attachments);
    }

    public void setYLevel(int yLevel) {
        this.yLevel = yLevel;
        QuarryPlus.LOGGER.debug("Quarry yLevel is set to " + yLevel + ".");
    }

    private BI S_addDroppedItems(final Collection<ItemStack> collection, final IBlockState state, final BlockPos pos) {
        Block block = state.getBlock();
        byte i;
        int xp = 0;
        QuarryFakePlayer fakePlayer = QuarryFakePlayer.get(((WorldServer) world));
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, getEnchantedPickaxe());
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            Set<ItemStack> rawItems;
            if (block.canSilkHarvest(state, world, pos, fakePlayer) && this.silktouch
                && silktouchList.contains(new BlockData(state)) == this.silktouchInclude) {
                NonNullList<ItemStack> list = NonNullList.create();
                list.add(invoke(createStackedBlock, ItemStack.class, block, state));
                rawItems = new HashSet<>(list);
                ForgeEventFactory.fireBlockHarvesting(list, world, pos, state, 0, 1.0f, true, fakePlayer);
                collection.addAll(list);
                i = -1;
            } else {
                boolean b = fortuneList.contains(new BlockData(state)) == this.fortuneInclude;
                byte fortuneLevel = b ? this.fortune : 0;
                NonNullList<ItemStack> list = NonNullList.create();
                block.getDrops(state, list, world, pos, (int) fortuneLevel);

                //                if (list.isEmpty() && Config.content().debug())
//                    ReflectionHelper.checkGetDrops(world, pos, state, block, fortuneLevel, list);
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
                    xp += getSmeltingXp(collection, rawItems, world);
                }
            }
        } else {
            i = -2;
        }
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        return new BI(i, xp, facingMap.containsKey(IAttachment.Attachments.REPLACER));
    }

    /**
     * @param stacks read only
     * @param raw    read only
     * @param world  need to get Recipe Manager.
     * @return The amount of xp by smelting items.
     */
    public static int getSmeltingXp(Collection<ItemStack> stacks, Collection<ItemStack> raw, World world) {
        InventoryBasic basic = new InventoryBasic(new TextComponentString("Dummy"), 2);

        return stacks.stream().filter(not(raw::contains)).mapToInt(stack -> {
            basic.setInventorySlotContents(0, stack);
            Optional<FurnaceRecipe> recipe = Optional.ofNullable(world.getRecipeManager().getRecipe(basic, world, VanillaRecipeTypes.SMELTING));
            return floorFloat(recipe.map(FurnaceRecipe::getExperience).orElse(0f) * stack.getCount());
        }).sum();
    }

    public static int floorFloat(float value) {
        int i = MathHelper.floor(value);
        return i + (Math.random() < (value - i) ? 1 : 0);
    }

    public static final Method createStackedBlock = ObfuscationReflectionHelper.findMethod(Block.class, "func_180643_i", IBlockState.class);

    @Override
    public void read(final NBTTagCompound nbt) {
        super.read(nbt);
        this.silktouch = nbt.getBoolean("silktouch");
        this.fortune = nbt.getByte("fortune");
        this.efficiency = nbt.getByte("efficiency");
        this.unbreaking = nbt.getByte("unbreaking");
        this.fortuneInclude = nbt.getBoolean("fortuneInclude");
        this.silktouchInclude = nbt.getBoolean("silktouchInclude");
        this.yLevel = Math.max(nbt.getInt("yLevel"), 1);
        fortuneList = nbt.getList("fortuneList", Constants.NBT.TAG_COMPOUND).stream().map(NBTTagCompound.class::cast)
            .map(BlockData::read).collect(Collectors.toCollection(NoDuplicateList::create));
        silktouchList = nbt.getList("silktouchList", Constants.NBT.TAG_COMPOUND).stream().map(NBTTagCompound.class::cast)
            .map(BlockData::read).collect(Collectors.toCollection(NoDuplicateList::create));
        ench = nbt.getList("enchList", Constants.NBT.TAG_COMPOUND).stream().map(NBTTagCompound.class::cast)
            .map(toEntry(n -> n.getString("id"), n -> n.getInt("value")))
            .map(keys(ResourceLocation::new))
            .filter(byKey(ForgeRegistries.ENCHANTMENTS::containsKey))
            .collect(entryToMap());
    }

    @Override
    public NBTTagCompound write(final NBTTagCompound nbt) {
        nbt.putBoolean("silktouch", this.silktouch);
        nbt.putByte("fortune", this.fortune);
        nbt.putByte("efficiency", this.efficiency);
        nbt.putByte("unbreaking", this.unbreaking);
        nbt.putBoolean("fortuneInclude", this.fortuneInclude);
        nbt.putBoolean("silktouchInclude", this.silktouchInclude);
        nbt.put("fortuneList", fortuneList.stream().map(BlockData.dataToNbt()::apply).collect(Collectors.toCollection(NBTTagList::new)));
        nbt.put("silktouchList", silktouchList.stream().map(BlockData.dataToNbt()::apply).collect(Collectors.toCollection(NBTTagList::new)));
        nbt.putInt("yLevel", this.yLevel);

        nbt.put("enchList", ench.entrySet().stream().map(keys(ResourceLocation::toString)).map(values(NBTTagInt::new)).collect(NBTBuilder.toNBTTag()));
        return super.write(nbt);
    }

    @Nonnull
    @Override
    public Map<ResourceLocation, Integer> getEnchantments() {
        Map<ResourceLocation, Integer> ret = new HashMap<>(ench);
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
    public void setEnchantment(ResourceLocation id, short val) {
        if (id.equals(EfficiencyID))
            this.efficiency = (byte) val;
        else if (id.equals(FortuneID))
            this.fortune = (byte) val;
        else if (id.equals(UnbreakingID))
            this.unbreaking = (byte) val;
        else if (id.equals(SilktouchID))
            this.silktouch = val > 0;

        if (val > 0) {
            ench.put(id, (int) val);
        }
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
    public ItemStack decrStackSize(final int index, final int count) {
        if (index < 0 || index >= this.cacheItems.size())
            return ItemStack.EMPTY;
        final ItemStack from = this.cacheItems.get(index);
        final ItemStack res = new ItemStack(from.getItem(), Math.min(count, from.getCount()));
        Optional.ofNullable(from.getTag()).map(NBTTagCompound::copy).ifPresent(res::setTag);
        from.shrink(res.getCount());
        if (from.isEmpty())
            this.cacheItems.remove(index);
        return res;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        return index < 0 || index >= this.cacheItems.size() ? ItemStack.EMPTY : this.cacheItems.remove(index);
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        if (!stack.isEmpty())
            QuarryPlus.LOGGER.warn("QuarryPlus WARN: call setInventorySlotContents with non null ItemStack.");
        removeStackFromSlot(index);
    }

    @Override
    public ITextComponent getName() {
        return new TextComponentString(TranslationKeys.MACHINE_BUFFER);
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> handler));
        }
        return super.getCapability(cap, side);
    }
//    @Override
//    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
//    public final ConnectionType canConnectInventory(EnumFacing from) {
//        return ConnectionType.FORCE;
//    }

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
