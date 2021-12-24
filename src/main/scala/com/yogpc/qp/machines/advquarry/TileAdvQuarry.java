package com.yogpc.qp.machines.advquarry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.BreakResult;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentHolder;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.ItemConverter;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerConfig;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QuarryFakePlayer;
import com.yogpc.qp.machines.module.ContainerQuarryModule;
import com.yogpc.qp.machines.module.ModuleInventory;
import com.yogpc.qp.machines.module.QuarryModule;
import com.yogpc.qp.machines.module.QuarryModuleProvider;
import com.yogpc.qp.machines.module.ReplacerModule;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.CacheEntry;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class TileAdvQuarry extends PowerTile implements
    CheckerLog, ModuleInventory.HasModuleInventory, MachineStorage.HasStorage,
    EnchantmentLevel.HasEnchantments, ClientSync, MenuProvider, PowerConfig.Provider {

    // Inventory
    private final ModuleInventory moduleInventory = new ModuleInventory(5, this::updateModule, TileAdvQuarry::moduleFilter, this);
    private Set<QuarryModule> modules = Set.of();
    private boolean isBlockModuleLoaded = false;
    private final MachineStorage storage = new MachineStorage();

    // Work
    private final QuarryCache cache = new QuarryCache();
    private final ItemConverter itemConverter = ItemConverter.defaultConverter().combined(ItemConverter.advQuarryConverter());
    public int digMinY;
    @Nullable
    Area area = null;
    private List<EnchantmentLevel> enchantments = List.of();
    private AdvQuarryAction action = AdvQuarryAction.Waiting.WAITING;

    public TileAdvQuarry(BlockPos pos, BlockState state) {
        super(Holder.ADV_QUARRY_TYPE, pos, state);
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "%sArea:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, area),
            "%sAction:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, action),
            "%sRemoveBedrock:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, hasBedrockModule()),
            "%sDigMinY:%s %d".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, digMinY),
            "%sModules:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, modules),
            "%sEnergy:%s %f/%d FE (%d)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getMaxEnergyStored(), getEnergy())
        ).map(TextComponent::new).toList();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
        if (!quarry.isBlockModuleLoaded) {
            quarry.updateModule();
            quarry.isBlockModuleLoaded = true;
        }
        if (quarry.hasEnoughEnergy()) {
            quarry.action.tick(world, pos, state, quarry);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (area != null) {
            var bottom = this.level == null ? 0 : level.getMinBuildHeight();
            return new AABB(area.minX(), bottom, area.minZ(), area.maxX(), area.maxY(), area.maxZ());
        } else {
            return new AABB(getBlockPos(), getBlockPos().offset(1, 1, 1));
        }
    }

    @Override
    public void saveNbtData(CompoundTag nbt) {
        nbt.put("moduleInventory", moduleInventory.serializeNBT());
        nbt.put("storage", storage.toNbt());
        toClientTag(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return serializeNBT();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        moduleInventory.deserializeNBT(nbt.getCompound("moduleInventory"));
        storage.readNbt(nbt.getCompound("storage"));
        fromClientTag(nbt);
        isBlockModuleLoaded = false;
    }

    @Override
    public CompoundTag toClientTag(CompoundTag nbt) {
        if (area != null) nbt.put("area", area.toNBT());
        var enchantments = new CompoundTag();
        this.enchantments.forEach(e -> enchantments.putInt(Objects.requireNonNull(e.enchantmentID()).toString(), e.level()));
        nbt.put("enchantments", enchantments);
        nbt.putInt("digMinY", digMinY);
        nbt.put("action", action.toNbt());
        return nbt;
    }

    @Override
    public void fromClientTag(CompoundTag nbt) {
        area = Area.fromNBT(nbt.getCompound("area")).orElse(null);
        var enchantments = nbt.getCompound("enchantments");
        setEnchantments(enchantments.getAllKeys().stream()
            .mapMulti(MapMulti.getEntry(ForgeRegistries.ENCHANTMENTS, enchantments::getInt))
            .map(EnchantmentLevel::new)
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
            .toList());
        digMinY = nbt.getInt("digMinY");
        action = AdvQuarryAction.fromNbt(nbt.getCompound("action"), this);
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

    void initialSetting(List<EnchantmentLevel> enchantments) {
        setEnchantments(enchantments);
        if (this.level != null) {
            this.digMinY = level.getMinBuildHeight();
        }
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
                logUsage();
            }
        if (level != null && !level.isClientSide) {
            PacketHandler.sendToClient(new ClientSyncMessage(this), level);
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

    BlockState getReplacementState() {
        return cache.replaceState.getValue(level);
    }

    @Override
    public ModuleInventory getModuleInventory() {
        return moduleInventory;
    }

    void updateModule() {
        // Blocks
        Set<QuarryModule> blockModules = level != null
            ? QuarryModuleProvider.Block.getModulesInWorld(level, getBlockPos())
            : Collections.emptySet();

        // Module Inventory
        var itemModules = moduleInventory.getModules();
        this.modules = Stream.concat(blockModules.stream(), itemModules.stream()).collect(Collectors.toSet());
    }

    static boolean moduleFilter(QuarryModule module) {
        return module != QuarryModule.Constant.PUMP;
    }

    @Override
    public Set<QuarryModule> getLoadedModules() {
        return modules;
    }

    @Override
    public MachineStorage getStorage() {
        return storage;
    }

    @Override
    public AdvQuarryMenu createMenu(int id, Inventory p, Player player) {
        return new AdvQuarryMenu(id, player, getBlockPos());
    }

    public ServerLevel getTargetWorld() {
        return (ServerLevel) this.level;
    }

    @SuppressWarnings("DuplicatedCode")
    public BreakResult breakOneBlock(BlockPos targetPos, boolean requireEnergy) {
        var targetWorld = getTargetWorld();
        var pickaxe = getPickaxe();
        var fakePlayer = QuarryFakePlayer.get(targetWorld);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        // Check breakable
        var state = targetWorld.getBlockState(targetPos);
        var breakEvent = new BlockEvent.BreakEvent(targetWorld, targetPos, state, fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        if (breakEvent.isCanceled()) {
            return BreakResult.FAIL_EVENT;
        }
        if (state.isAir() || !canBreak(targetWorld, targetPos, state)) {
            return BreakResult.SKIPPED;
        }

        // Break block
        var hardness = state.getDestroySpeed(targetWorld, targetPos);
        if (requireEnergy && !useEnergy(PowerManager.getBreakEnergy(hardness, this), Reason.BREAK_BLOCK, false)) {
            return BreakResult.NOT_ENOUGH_ENERGY;
        }
        // Get drops
        var drops = Block.getDrops(state, targetWorld, targetPos, targetWorld.getBlockEntity(targetPos), fakePlayer, pickaxe);
        drops.stream().map(itemConverter::map).forEach(this.storage::addItem);
        targetWorld.setBlock(targetPos, getReplacementState(), Block.UPDATE_ALL);
        // Get experiments
        if (breakEvent.getExpToDrop() > 0) {
            getExpModule().ifPresent(e -> {
                if (requireEnergy)
                    useEnergy(PowerManager.getExpCollectEnergy(breakEvent.getExpToDrop(), this), Reason.EXP_COLLECT, true);
                e.addExp(breakEvent.getExpToDrop());
            });
        }
        setChanged();
        return BreakResult.SUCCESS;
    }

    BreakResult breakBlocks(int x, int z) {
        var targetWorld = getTargetWorld();
        var pickaxe = getPickaxe();
        var fakePlayer = QuarryFakePlayer.get(targetWorld);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        var aabb = new AABB(x - 5, digMinY - 5, z - 5, x + 5, getBlockPos().getY() - 1, z + 5);
        targetWorld.getEntitiesOfClass(ItemEntity.class, aabb, Predicate.not(i -> i.getItem().isEmpty()))
            .forEach(i -> {
                storage.addItem(i.getItem());
                i.kill();
            });
        getExpModule().ifPresent(e ->
            targetWorld.getEntitiesOfClass(ExperienceOrb.class, aabb, EntitySelector.ENTITY_STILL_ALIVE)
                .forEach(orb -> {
                    e.addExp(orb.getValue());
                    orb.kill();
                }));
        removeEdgeFluid(x, z, targetWorld);
        long requiredEnergy = 0;
        var exp = new AtomicInteger(0);
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
                var breakEvent = new BlockEvent.BreakEvent(targetWorld, mutableBlockPos, state, fakePlayer);
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (breakEvent.isCanceled()) {
                    continue; // Not breakable. Ignore.
                }
                exp.getAndAdd(breakEvent.getExpToDrop());
                // Calc required energy
                var hardness = state.getDestroySpeed(targetWorld, mutableBlockPos);
                var energy = PowerManager.getBreakEnergy(hardness, this);
                requiredEnergy += energy;
                toBreak.add(Pair.of(mutableBlockPos.immutable(), state));
            } else {
                var energy = PowerManager.getBreakBlockFluidEnergy(this);
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
            targetWorld.setBlock(pair.getLeft(), Holder.BLOCK_DUMMY.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        // Get drops
        toBreak.stream().flatMap(p ->
                Block.getDrops(p.getRight(), targetWorld, p.getLeft(), targetWorld.getBlockEntity(p.getLeft()), fakePlayer, pickaxe).stream())
            .map(itemConverter::map).forEach(this.storage::addItem);
        // Remove blocks
        toBreak.stream().map(Pair::getLeft)
            .forEach(p -> targetWorld.setBlock(p, getReplacementState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE));
        if (exp.get() > 0) {
            getExpModule().ifPresent(e -> {
                useEnergy(PowerManager.getExpCollectEnergy(exp.get(), this), Reason.EXP_COLLECT, true);
                e.addExp(exp.get());
            });
        }
        setChanged();
        return BreakResult.SUCCESS;
    }

    void removeEdgeFluid(int x, int z, ServerLevel targetWorld) {
        assert area != null;
        boolean flagMinX = x - 1 == area.minX();
        boolean flagMaxX = x + 1 == area.maxX();
        boolean flagMinZ = z - 1 == area.minZ();
        boolean flagMaxZ = z + 1 == area.maxZ();
        if (flagMinX) {
            removeFluidAtXZ(area.minX(), z, targetWorld);
        }
        if (flagMaxX) {
            removeFluidAtXZ(area.maxX(), z, targetWorld);
        }
        if (flagMinZ) {
            removeFluidAtXZ(x, area.minZ(), targetWorld);
        }
        if (flagMaxZ) {
            removeFluidAtXZ(x, area.maxZ(), targetWorld);
        }
        if (flagMinX && flagMinZ) {
            removeFluidAtXZ(area.minX(), area.minZ(), targetWorld);
        }
        if (flagMinX && flagMaxZ) {
            removeFluidAtXZ(area.minX(), area.maxZ(), targetWorld);
        }
        if (flagMaxX && flagMinZ) {
            removeFluidAtXZ(area.maxX(), area.minZ(), targetWorld);
        }
        if (flagMaxX && flagMaxZ) {
            removeFluidAtXZ(area.maxX(), area.maxZ(), targetWorld);
        }
    }

    void removeFluidAtXZ(int x, int z, ServerLevel world) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = getBlockPos().getY() - 1; y > digMinY; y--) {
            pos.setY(y);
            var fluidState = world.getFluidState(pos);
            if (!fluidState.isEmpty()) {
                var state = world.getBlockState(pos);
                if (state.getBlock() instanceof BucketPickup fluidBlock) {
                    useEnergy(PowerManager.getBreakBlockFluidEnergy(this), Reason.REMOVE_FLUID, true);
                    var bucketItem = fluidBlock.pickupBlock(world, pos, state);
                    storage.addFluid(bucketItem);
                    if (world.getBlockState(pos).isAir() || (fluidBlock instanceof LiquidBlock && !fluidState.isSource())) {
                        world.setBlock(pos, Holder.BLOCK_FRAME.getDammingState(), Block.UPDATE_ALL);
                    }
                } else if (state.getBlock() instanceof LiquidBlockContainer) {
                    float hardness = state.getDestroySpeed(world, pos);
                    useEnergy(PowerManager.getBreakEnergy(hardness, this), Reason.REMOVE_FLUID, true);
                    var drops = Block.getDrops(state, world, pos, world.getBlockEntity(pos), null, this.getPickaxe());
                    drops.forEach(this.storage::addItem);
                    world.setBlock(pos, Holder.BLOCK_FRAME.getDammingState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    void openModuleGui(ServerPlayer player) {
        ContainerQuarryModule.InteractionObject.openGUI(this, player, getDisplayName());
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
            replaceState = CacheEntry.supplierCache(5,
                () -> TileAdvQuarry.this.getReplacerModule().map(ReplacerModule::getState).orElse(Blocks.AIR.defaultBlockState()));
            netherTop = CacheEntry.supplierCache(100,
                QuarryPlus.config.common.netherTop::get);
            enchantments = CacheEntry.supplierCache(1000, () -> EnchantmentHolder.makeHolder(TileAdvQuarry.this));
        }
    }

}
