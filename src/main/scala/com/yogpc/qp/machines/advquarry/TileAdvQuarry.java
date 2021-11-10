package com.yogpc.qp.machines.advquarry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.BreakResult;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentHolder;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.ItemConverter;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.CacheEntry;
import com.yogpc.qp.utils.MapMulti;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.Pair;

public class TileAdvQuarry extends PowerTile implements
    CheckerLog, MachineStorage.HasStorage,
    EnchantmentLevel.HasEnchantments, BlockEntityClientSerializable, ExtendedScreenHandlerFactory {

    // Inventory
    private boolean isBlockModuleLoaded = false;
    private final MachineStorage storage = new MachineStorage();

    // Work
    private final QuarryCache cache = new QuarryCache();
    private final ItemConverter itemConverter = ItemConverter.defaultConverter().combined(ItemConverter.advQuarryConverter());
    public int digMinY;
    private boolean removeBedrock;
    @Nullable
    Area area = null;
    private List<EnchantmentLevel> enchantments = List.of();
    private AdvQuarryAction action = AdvQuarryAction.Waiting.WAITING;

    public TileAdvQuarry(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.ADV_QUARRY_TYPE, pos, state);
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "%sArea:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, area),
            "%sAction:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, action),
            "%sRemoveBedrock:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, hasBedrockModule()),
            "%sDigMinY:%s %d".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, digMinY),
            "%sEnergy:%s %f/%d FE (%d)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getMaxEnergy() / PowerTile.ONE_FE, getEnergy())
        ).map(TextComponent::new).toList();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
        if (!quarry.isBlockModuleLoaded) {
            quarry.isBlockModuleLoaded = true;
        }
        if (quarry.hasEnoughEnergy()) {
            quarry.action.tick(world, pos, state, quarry);
        }
    }

    public AABB getRenderBoundingBox() {
        if (area != null)
            return new AABB(area.minX(), 0, area.minZ(), area.maxX(), area.maxY(), area.maxZ());
        else
            return new AABB(getBlockPos(), getBlockPos().offset(1, 1, 1));
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("storage", storage.toNbt());
        toClientTag(nbt);
        nbt.putInt("digMinY", digMinY);
        return super.save(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        storage.readNbt(nbt.getCompound("storage"));
        fromClientTag(nbt);
        digMinY = nbt.getInt("digMinY");
        isBlockModuleLoaded = false;
    }

    @Override
    public CompoundTag toClientTag(CompoundTag nbt) {
        if (area != null) nbt.put("area", area.toNBT());
        var enchantments = new CompoundTag();
        this.enchantments.forEach(e -> enchantments.putInt(Objects.requireNonNull(e.enchantmentID()).toString(), e.level()));
        nbt.put("enchantments", enchantments);
        nbt.put("action", action.toNbt());
        nbt.putBoolean("bedrockRemove", removeBedrock);
        return nbt;
    }

    @Override
    public void fromClientTag(CompoundTag nbt) {
        area = Area.fromNBT(nbt.getCompound("area")).orElse(null);
        var enchantments = nbt.getCompound("enchantments");
        setEnchantments(enchantments.getAllKeys().stream()
            .mapMulti(MapMulti.getEntry(Registry.ENCHANTMENT, enchantments::getInt))
            .map(EnchantmentLevel::new)
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
            .toList());
        action = AdvQuarryAction.fromNbt(nbt.getCompound("action"), this);
        removeBedrock = nbt.getBoolean("bedrockRemove");
    }

    /**
     * Set enchantment of this machine.
     *
     * @param enchantments should be sorted with {@link EnchantmentLevel#QUARRY_ENCHANTMENT_COMPARATOR}
     */
    public void setEnchantments(List<EnchantmentLevel> enchantments) {
        this.enchantments = enchantments;
        this.cache.enchantments.expire();
        this.setMaxEnergy(50000 * ONE_FE * (efficiencyLevel() + 1));
    }

    @Nullable
    public Area getArea() {
        return area;
    }

    public AdvQuarryAction getAction() {
        return action;
    }

    void setAction(AdvQuarryAction action) {
        var pre = this.action;
        if (this.action == AdvQuarryAction.Waiting.WAITING)
            if (level != null) {
                level.setBlock(getBlockPos(), getBlockState().setValue(BlockAdvQuarry.WORKING, true), Block.UPDATE_ALL);
            }
        this.action = action;
        if (action == AdvQuarryAction.Finished.FINISHED)
            if (level != null) {
                level.setBlock(getBlockPos(), getBlockState().setValue(BlockAdvQuarry.WORKING, false), Block.UPDATE_ALL);
                logUsage(QuarryPlus.config.common.debug ? QuarryPlus.LOGGER::info : QuarryPlus.LOGGER::debug);
            }
        if (level != null && !level.isClientSide) {
            sync();
            QuarryPlus.LOGGER.debug("ChunkDestroyer({}) State changed from {} to {}.", getBlockPos(), pre, action);
        }
    }

    @Override
    public List<EnchantmentLevel> getEnchantments() {
        return this.enchantments;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canBreak(Level targetWorld, BlockPos targetPos, BlockState state) {
        if (state.isAir()) return true;
        var unbreakable = state.getDestroySpeed(targetWorld, targetPos) < 0;
        if (unbreakable) {
            if (hasBedrockModule() && state.getBlock() == Blocks.BEDROCK) {
                return !targetWorld.dimension().equals(Level.END);
            } else {
                return false;
            }
        } else if (!targetWorld.getFluidState(targetPos).isEmpty()) {
            return true;
        } else {
            return getReplacementState() != state;
        }
    }

    private boolean hasBedrockModule() {
        return removeBedrock;
    }

    BlockState getReplacementState() {
        return cache.replaceState.getValue(level);
    }

    @Override
    public MachineStorage getStorage() {
        return storage;
    }

    @Override
    public AdvQuarryMenu createMenu(int id, Inventory p, Player player) {
        return new AdvQuarryMenu(id, player, getBlockPos());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(getBlockPos());
    }

    public ServerLevel getTargetWorld() {
        return (ServerLevel) this.level;
    }

    @SuppressWarnings("DuplicatedCode")
    public BreakResult breakOneBlock(BlockPos targetPos, boolean requireEnergy) {
        var targetWorld = getTargetWorld();
        var pickaxe = getPickaxe();
        // Check breakable
        var state = targetWorld.getBlockState(targetPos);
        if (state.isAir() || !canBreak(targetWorld, targetPos, state)) {
            return BreakResult.SKIPPED;
        }

        // Break block
        var hardness = state.getDestroySpeed(targetWorld, targetPos);
        if (requireEnergy && !useEnergy(Constants.getBreakEnergy(hardness, this), Reason.BREAK_BLOCK, false)) {
            return BreakResult.NOT_ENOUGH_ENERGY;
        }
        // Get drops
        var drops = Block.getDrops(state, targetWorld, targetPos, targetWorld.getBlockEntity(targetPos), null, pickaxe);
        drops.stream().map(itemConverter::map).forEach(this.storage::addItem);
        targetWorld.setBlock(targetPos, getReplacementState(), Block.UPDATE_ALL);
        setChanged();
        return BreakResult.SUCCESS;
    }

    BreakResult breakBlocks(int x, int z) {
        var targetWorld = getTargetWorld();
        var pickaxe = getPickaxe();
        var aabb = new AABB(x - 5, digMinY - 5, z - 5, x + 5, getBlockPos().getY() - 1, z + 5);
        targetWorld.getEntitiesOfClass(ItemEntity.class, aabb, Predicate.not(i -> i.getItem().isEmpty()))
            .forEach(i -> {
                storage.addItem(i.getItem());
                i.kill();
            });
        checkEdgeFluid(x, z, targetWorld);
        long requiredEnergy = 0;
        List<Pair<BlockPos, BlockState>> toBreak = new ArrayList<>();
        List<Pair<BlockPos, BlockState>> toDrain = new ArrayList<>();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = getBlockPos().getY() - 1; y > digMinY; y--) {
            mutableBlockPos.setY(y);
            var state = targetWorld.getBlockState(mutableBlockPos);
            var fluidState = targetWorld.getFluidState(mutableBlockPos);
            if (fluidState.isEmpty()) {
                if (state.isAir() || !canBreak(targetWorld, mutableBlockPos, state))
                    continue;
                // Calc required energy
                var hardness = state.getDestroySpeed(targetWorld, mutableBlockPos);
                var energy = Constants.getBreakEnergy(hardness, this);
                requiredEnergy += energy;
                toBreak.add(Pair.of(mutableBlockPos.immutable(), state));
            } else {
                var energy = Constants.getBreakBlockFluidEnergy(this);
                requiredEnergy += energy;
                toDrain.add(Pair.of(mutableBlockPos.immutable(), state));
            }
        }
        if (toBreak.isEmpty() && toDrain.isEmpty()) return BreakResult.SKIPPED;
        useEnergy(requiredEnergy, Reason.BREAK_BLOCK, true);

        // Drain fluids
        for (Pair<BlockPos, BlockState> pair : toDrain) {
            if (pair.getRight().getBlock() instanceof BucketPickup fluidBlock) {
                var bucketItem = fluidBlock.pickupBlock(targetWorld, pair.getLeft(), pair.getRight());
                storage.addFluid(bucketItem);
            }
            var state = targetWorld.getBlockState(pair.getLeft());
            if (!state.isAir() && canBreak(targetWorld, pair.getLeft(), state)) {
                breakOneBlock(pair.getLeft(), false);
            }
            targetWorld.setBlock(pair.getLeft(), QuarryPlus.ModObjects.BLOCK_DUMMY.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        // Get drops
        toBreak.stream().flatMap(p ->
                Block.getDrops(p.getRight(), targetWorld, p.getLeft(), targetWorld.getBlockEntity(p.getLeft()), null, pickaxe).stream())
            .map(itemConverter::map).forEach(this.storage::addItem);
        // Remove blocks
        toBreak.stream().map(Pair::getLeft)
            .forEach(p -> targetWorld.setBlock(p, getReplacementState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE));

        setChanged();
        return BreakResult.SUCCESS;
    }

    void checkEdgeFluid(int x, int z, ServerLevel targetWorld) {
        assert area != null;
        boolean flagMinX = x - 1 == area.minX();
        boolean flagMaxX = x + 1 == area.maxX();
        boolean flagMinZ = z - 1 == area.minZ();
        boolean flagMaxZ = z + 1 == area.maxZ();
        if (flagMinX) {
            removeFluidAtEdge(area.minX(), z, targetWorld);
        }
        if (flagMaxX) {
            removeFluidAtEdge(area.maxX(), z, targetWorld);
        }
        if (flagMinZ) {
            removeFluidAtEdge(x, area.minZ(), targetWorld);
        }
        if (flagMaxZ) {
            removeFluidAtEdge(x, area.maxZ(), targetWorld);
        }
        if (flagMinX && flagMinZ) {
            removeFluidAtEdge(area.minX(), area.minZ(), targetWorld);
        }
        if (flagMinX && flagMaxZ) {
            removeFluidAtEdge(area.minX(), area.maxZ(), targetWorld);
        }
        if (flagMaxX && flagMinZ) {
            removeFluidAtEdge(area.maxX(), area.minZ(), targetWorld);
        }
        if (flagMaxX && flagMaxZ) {
            removeFluidAtEdge(area.maxX(), area.maxZ(), targetWorld);
        }
    }

    void removeFluidAtEdge(int x, int z, ServerLevel world) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = getBlockPos().getY() - 1; y > digMinY; y--) {
            pos.setY(y);
            var fluidState = world.getFluidState(pos);
            if (!fluidState.isEmpty()) {
                var state = world.getBlockState(pos);
                if (state.getBlock() instanceof BucketPickup fluidBlock) {
                    useEnergy(Constants.getBreakBlockFluidEnergy(this), Reason.REMOVE_FLUID, true);
                    var bucketItem = fluidBlock.pickupBlock(world, pos, state);
                    storage.addFluid(bucketItem);
                    if (world.getBlockState(pos).isAir() || (fluidBlock instanceof LiquidBlock && !fluidState.isSource())) {
                        world.setBlock(pos, QuarryPlus.ModObjects.BLOCK_FRAME.getDammingState(), Block.UPDATE_ALL);
                    }
                } else if (state.getBlock() instanceof LiquidBlockContainer) {
                    float hardness = state.getDestroySpeed(world, pos);
                    useEnergy(Constants.getBreakEnergy(hardness, this), Reason.REMOVE_FLUID, true);
                    var drops = Block.getDrops(state, world, pos, world.getBlockEntity(pos), null, this.getPickaxe());
                    drops.forEach(this.storage::addItem);
                    world.setBlock(pos, QuarryPlus.ModObjects.BLOCK_FRAME.getDammingState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public int efficiencyLevel() {
        return cache.enchantments.getValue(getLevel()).efficiency();
    }

    @Override
    public int unbreakingLevel() {
        return cache.enchantments.getValue(getLevel()).unbreaking();
    }

    @Override
    public int fortuneLevel() {
        return cache.enchantments.getValue(getLevel()).fortune();
    }

    @Override
    public int silktouchLevel() {
        return cache.enchantments.getValue(getLevel()).silktouch();
    }

    private class QuarryCache {
        final CacheEntry<BlockState> replaceState;
        final CacheEntry<Integer> netherTop;
        final CacheEntry<EnchantmentHolder> enchantments;

        public QuarryCache() {
            replaceState = CacheEntry.constantCache(
                Blocks.AIR::defaultBlockState);
            netherTop = CacheEntry.supplierCache(100,
                () -> QuarryPlus.config.common.netherTop);
            enchantments = CacheEntry.supplierCache(1000, () -> EnchantmentHolder.makeHolder(TileAdvQuarry.this));
        }
    }

}
