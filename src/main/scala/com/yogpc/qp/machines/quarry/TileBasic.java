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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.machines.workbench.BlockData;
import com.yogpc.qp.utils.NBTBuilder;
import com.yogpc.qp.utils.NoDuplicateList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import static com.yogpc.qp.machines.base.IAttachment.Attachments.ALL;
import static com.yogpc.qp.machines.base.IAttachment.Attachments.EXP_PUMP;
import static com.yogpc.qp.machines.base.IAttachment.Attachments.FLUID_PUMP;
import static com.yogpc.qp.machines.base.IEnchantableItem.FORTUNE;
import static com.yogpc.qp.machines.base.IEnchantableItem.SILKTOUCH;
import static jp.t2v.lab.syntax.MapStreamSyntax.byKey;
import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.keys;
import static jp.t2v.lab.syntax.MapStreamSyntax.not;
import static jp.t2v.lab.syntax.MapStreamSyntax.toAny;
import static jp.t2v.lab.syntax.MapStreamSyntax.toEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;

//@net.minecraftforge.fml.common.Optional.Interface(iface = "cofh.api.tileentity.IInventoryConnection", modid = QuarryPlus.Optionals.COFH_modID)
public abstract class TileBasic extends APowerTile implements IEnchantableTile, HasInv, IAttachable/*, IInventoryConnection*/ {

    private static final Set<IAttachment.Attachments<?>> VALID_ATTACHMENTS = ALL;
    protected final Map<IAttachment.Attachments<?>, Direction> facingMap = new HashMap<>();

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

    public List<IModule> modules = Collections.emptyList();

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
        assert world != null;
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
    protected boolean S_breakBlock(final int x, final int y, final int z, BlockState replace) {
        assert world != null;
        final List<ItemStack> dropped = new ArrayList<>(2);
        final BlockState blockState;
        BlockPos pos = new BlockPos(x, y, z);
        blockState = world.getBlockState(pos);
        if (QuarryBlackList.contains(blockState, world, pos))
            return true;

        BI bi = S_addDroppedItems(dropped, blockState, pos);
        modules.forEach(iModule -> iModule.invoke(new IModule.BeforeBreak(bi.i, world, pos)));
        if (!PowerManager.useEnergyBreak(this, blockState.getBlockHardness(world, pos), bi.b, this.unbreaking, bi.b1, true))
            return false;
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
    public boolean connectAttachment(final Direction facing, IAttachment.Attachments<? extends APacketTile> attachments, boolean simulate) {
        assert world != null;
        if (facingMap.containsKey(attachments)) {
            TileEntity entity = world.getTileEntity(getPos().offset(facingMap.get(attachments)));
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
        QuarryPlus.LOGGER.debug("Quarry yLevel is set to " + yLevel + ".");
    }

    private BI S_addDroppedItems(final Collection<ItemStack> collection, final BlockState state, final BlockPos pos) {
        assert world != null;
        byte i;
        int xp = 0;
        QuarryFakePlayer fakePlayer = QuarryFakePlayer.get(((ServerWorld) world), pos);
        ItemStack pickaxe;
        if (this.silktouch && silktouchList.contains(new BlockData(state)) == this.silktouchInclude) {
            pickaxe = getEnchantedPickaxe(FORTUNE.negate());
            i = -1;
        } else if (fortuneList.contains(new BlockData(state)) == this.fortuneInclude) {
            pickaxe = getEnchantedPickaxe(SILKTOUCH.negate());
            i = this.fortune;
        } else {
            pickaxe = getEnchantedPickaxe(SILKTOUCH.negate().and(FORTUNE.negate()));
            i = 0;
        }
        fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            Set<ItemStack> rawItems;

            NonNullList<ItemStack> list = NonNullList.create();
            list.addAll(Block.getDrops(state, ((ServerWorld) world), pos, world.getTileEntity(pos), fakePlayer, fakePlayer.getHeldItem(Hand.MAIN_HAND)));

            rawItems = new HashSet<>(list);
            ForgeEventFactory.fireBlockHarvesting(list, world, pos, state, Math.max(0, i), 1.0f, i == -1, fakePlayer);
            collection.addAll(list);

            if (facingMap.containsKey(EXP_PUMP)) {
                xp += event.getExpToDrop();
                if (InvUtils.hasSmelting(fakePlayer.getHeldItemMainhand())) {
                    xp += getSmeltingXp(collection, rawItems, world);
                }
            }
        } else {
            i = -2;
        }
        fakePlayer.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        return new BI(i, xp, facingMap.containsKey(IAttachment.Attachments.REPLACER));
    }

    /**
     * @param stacks read only
     * @param raw    read only
     * @param world  need to get Recipe Manager.
     * @return The amount of xp by smelting items.
     */
    public static int getSmeltingXp(Collection<ItemStack> stacks, Collection<ItemStack> raw, World world) {
        Inventory basic = new Inventory(2);

        return stacks.stream().filter(not(raw::contains)).mapToInt(stack -> {
            basic.setInventorySlotContents(0, stack);
            Optional<FurnaceRecipe> recipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, basic, world);
            return floorFloat(recipe.map(FurnaceRecipe::getExperience).orElse(0f) * stack.getCount());
        }).sum();
    }

    public static int floorFloat(float value) {
        int i = MathHelper.floor(value);
        return i + (Math.random() < (value - i) ? 1 : 0);
    }

    @Override
    public void read(final CompoundNBT nbt) {
        super.read(nbt);
        this.silktouch = nbt.getBoolean("silktouch");
        this.fortune = nbt.getByte("fortune");
        this.efficiency = nbt.getByte("efficiency");
        this.unbreaking = nbt.getByte("unbreaking");
        this.fortuneInclude = nbt.getBoolean("fortuneInclude");
        this.silktouchInclude = nbt.getBoolean("silktouchInclude");
        this.yLevel = Math.max(nbt.getInt("yLevel"), 1);
        fortuneList = nbt.getList("fortuneList", Constants.NBT.TAG_COMPOUND).stream().map(CompoundNBT.class::cast)
            .map(BlockData::read).collect(Collectors.toCollection(NoDuplicateList::create));
        silktouchList = nbt.getList("silktouchList", Constants.NBT.TAG_COMPOUND).stream().map(CompoundNBT.class::cast)
            .map(BlockData::read).collect(Collectors.toCollection(NoDuplicateList::create));
        ench = nbt.getList("enchList", Constants.NBT.TAG_COMPOUND).stream().map(CompoundNBT.class::cast)
            .map(toEntry(n -> n.getString("id"), n -> n.getInt("value")))
            .map(keys(ResourceLocation::new))
            .filter(byKey(ForgeRegistries.ENCHANTMENTS::containsKey))
            .collect(entryToMap());
    }

    @Override
    public CompoundNBT write(final CompoundNBT nbt) {
        nbt.putBoolean("silktouch", this.silktouch);
        nbt.putByte("fortune", this.fortune);
        nbt.putByte("efficiency", this.efficiency);
        nbt.putByte("unbreaking", this.unbreaking);
        nbt.putBoolean("fortuneInclude", this.fortuneInclude);
        nbt.putBoolean("silktouchInclude", this.silktouchInclude);
        nbt.put("fortuneList", fortuneList.stream().map(BlockData.dataToNbt()::apply).collect(Collectors.toCollection(ListNBT::new)));
        nbt.put("silktouchList", silktouchList.stream().map(BlockData.dataToNbt()::apply).collect(Collectors.toCollection(ListNBT::new)));
        nbt.putInt("yLevel", this.yLevel);

        nbt.put("enchList", ench.entrySet().stream().map(keys(ResourceLocation::toString)).map(values(IntNBT::valueOf)).collect(NBTBuilder.toNBTTag()));
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
        Optional.ofNullable(from.getTag()).map(CompoundNBT::copy).ifPresent(res::setTag);
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
        return new StringTextComponent(TranslationKeys.MACHINE_BUFFER);
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
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
