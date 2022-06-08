package com.yogpc.qp.machines.quarry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
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
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.QuarryFakePlayer;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class TileQuarry extends PowerTile implements CheckerLog, MachineStorage.HasStorage,
    EnchantmentLevel.HasEnchantments, ClientSync, ModuleInventory.HasModuleInventory, PowerConfig.Provider {
    private static final Marker MARKER = MarkerManager.getMarker("TileQuarry");
    private static final EnchantmentRestriction RESTRICTION = EnchantmentRestriction.builder()
        .add(Enchantments.BLOCK_EFFICIENCY)
        .add(Enchantments.UNBREAKING)
        .add(Enchantments.BLOCK_FORTUNE)
        .add(Enchantments.SILK_TOUCH)
        .build();
    @Nullable
    public Target target;
    public QuarryState state = QuarryState.FINISHED;
    @Nullable
    private Area area;
    // May be unmodifiable
    private List<EnchantmentLevel> enchantments = new ArrayList<>();
    public final MachineStorage storage = new MachineStorage();
    public double headX, headY, headZ;
    private boolean init = false;
    public int digMinY = 0;
    private final ItemConverter itemConverter = ItemConverter.defaultConverter();
    private Set<QuarryModule> modules = new HashSet<>(); // May be immutable.
    private final ModuleInventory moduleInventory;
    private final QuarryCache cache = new QuarryCache();

    public TileQuarry(BlockPos pos, BlockState state) {
        super(Holder.QUARRY_TYPE, pos, state);
        this.moduleInventory = new ModuleInventory(5, this::updateModules, m -> true, this);
    }

    TileQuarry(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
        // This is SFQ so no module is acceptable.
        this.moduleInventory = new ModuleInventory(0, () -> {
        }, m -> false, this);
    }

    @Override
    public void saveNbtData(CompoundTag nbt) {
        if (target != null) {
            nbt.put("target", Target.toNbt(target));
        }
        nbt.putString("state", state.name());
        if (area != null)
            nbt.put("area", area.toNBT());
        {
            var enchantments = new CompoundTag();
            this.enchantments.forEach(e ->
                enchantments.putInt(Objects.requireNonNull(e.enchantmentID(), "Invalid enchantment. " + e.enchantment()).toString(), e.level()));
            nbt.put("enchantments", enchantments);
        }
        nbt.putDouble("headX", headX);
        nbt.putDouble("headY", headY);
        nbt.putDouble("headZ", headZ);
        nbt.put("storage", storage.toNbt());
        nbt.putInt("digMinY", digMinY);
        nbt.put("moduleInventory", moduleInventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        target = nbt.contains("target") ? Target.fromNbt(nbt.getCompound("target")) : null;
        state = QuarryState.valueOf(nbt.getString("state"));
        area = Area.fromNBT(nbt.getCompound("area")).orElse(null);
        {
            var enchantments = nbt.getCompound("enchantments");
            setEnchantments(enchantments.getAllKeys().stream()
                .mapMulti(MapMulti.getEntry(ForgeRegistries.ENCHANTMENTS, enchantments::getInt))
                .map(EnchantmentLevel::new)
                .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
                .toList());
        }
        headX = nbt.getDouble("headX");
        headY = nbt.getDouble("headY");
        headZ = nbt.getDouble("headZ");
        storage.readNbt(nbt.getCompound("storage"));
        digMinY = nbt.getInt("digMinY");
        moduleInventory.deserializeNBT(nbt.getCompound("moduleInventory"));
        init = true;
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        if (area != null)
            tag.put("area", area.toNBT());
        tag.putString("state", state.name());
        tag.putDouble("headX", headX);
        tag.putDouble("headY", headY);
        tag.putDouble("headZ", headZ);
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        area = Area.fromNBT(tag.getCompound("area")).orElse(null);
        state = QuarryState.valueOf(tag.getString("state"));
        headX = tag.getDouble("headX");
        headY = tag.getDouble("headY");
        headZ = tag.getDouble("headZ");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            this.init = true;
            updateModules();
        }
    }

    public void setArea(@Nullable Area area) {
        this.area = area;
        QuarryPlus.LOGGER.debug(MARKER, "{}({}) Area changed to {}.", getClass().getSimpleName(), getBlockPos(), area);
        if (area != null) {
            headX = area.maxX();
            headY = area.minY();
            headZ = area.maxZ();
        }
    }

    @Nullable
    public Area getArea() {
        return area;
    }

    public void setState(QuarryState quarryState, BlockState blockState) {
        if (this.state != quarryState) {
            var pre = this.state;
            this.state = quarryState;
            sync();
            if (level != null) {
                level.setBlock(getBlockPos(), blockState.setValue(QPBlock.WORKING, quarryState.isWorking), Block.UPDATE_ALL);
                if (!level.isClientSide && !quarryState.isWorking) {
                    logUsage();
                }
            }
            if ((pre != QuarryState.MOVE_HEAD && pre != QuarryState.BREAK_BLOCK && pre != QuarryState.REMOVE_FLUID) || quarryState == QuarryState.FILLER)
                QuarryPlus.LOGGER.debug(MARKER, "{}({}) State changed from {} to {}.", getClass().getSimpleName(), getBlockPos(), pre, quarryState);
        }
    }

    public ServerLevel getTargetWorld() {
        return (ServerLevel) getLevel();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TileQuarry quarry) {
        if (quarry.hasEnoughEnergy()) {
            if (quarry.init) {
                quarry.updateModules();
                quarry.init = false;
            }
            // In server world.
            quarry.state.tick(world, pos, state, quarry);
        }
    }

    public BreakResult breakBlock(BlockPos targetPos) {
        return breakBlock(targetPos, true);
    }

    @SuppressWarnings("DuplicatedCode")
    public BreakResult breakBlock(BlockPos targetPos, boolean requireEnergy) {
        var targetWorld = getTargetWorld();
        // Gather Drops
        if (targetPos.getX() % 3 == 0 && targetPos.getZ() % 3 == 0) {
            targetWorld.getEntitiesOfClass(ItemEntity.class, new AABB(targetPos).inflate(5), Predicate.not(i -> i.getItem().isEmpty()))
                .forEach(i -> {
                    storage.addItem(i.getItem());
                    i.kill();
                });
            getExpModule().ifPresent(e ->
                targetWorld.getEntitiesOfClass(ExperienceOrb.class, new AABB(targetPos).inflate(5), EntitySelector.ENTITY_STILL_ALIVE)
                    .forEach(orb -> {
                        e.addExp(orb.getValue());
                        orb.kill();
                    }));
        }
        var pickaxe = getPickaxe();
        var fakePlayer = QuarryFakePlayer.get(targetWorld);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        // Check breakable
        var state = targetWorld.getBlockState(targetPos);
        var breakEvent = new BlockEvent.BreakEvent(targetWorld, targetPos, state, fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        if (breakEvent.isCanceled()) {
            if (target != null) target.addSkipped(targetPos);
            return BreakResult.FAIL_EVENT;
        }
        if (state.isAir() || !canBreak(targetWorld, targetPos, state)) {
            return BreakResult.SKIPPED;
        }
        if (hasPumpModule()) removeEdgeFluid(targetPos, targetWorld, this);

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
        // Sound
        var sound = state.getSoundType();
        if (requireEnergy)
            targetWorld.playSound(null, targetPos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 4F, sound.getPitch() * 0.8F);

        return BreakResult.SUCCESS;
    }

    static void removeEdgeFluid(BlockPos targetPos, ServerLevel targetWorld, TileQuarry quarry) {
        var area = quarry.getArea();
        assert area != null;
        boolean flagMinX = targetPos.getX() - 1 == area.minX();
        boolean flagMaxX = targetPos.getX() + 1 == area.maxX();
        boolean flagMinZ = targetPos.getZ() - 1 == area.minZ();
        boolean flagMaxZ = targetPos.getZ() + 1 == area.maxZ();
        if (flagMinX) {
            removeFluidAtPos(targetWorld, new BlockPos(area.minX(), targetPos.getY(), targetPos.getZ()), quarry);
        }
        if (flagMaxX) {
            removeFluidAtPos(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), targetPos.getZ()), quarry);
        }
        if (flagMinZ) {
            removeFluidAtPos(targetWorld, new BlockPos(targetPos.getX(), targetPos.getY(), area.minZ()), quarry);
        }
        if (flagMaxZ) {
            removeFluidAtPos(targetWorld, new BlockPos(targetPos.getX(), targetPos.getY(), area.maxZ()), quarry);
        }
        if (flagMinX && flagMinZ) {
            removeFluidAtPos(targetWorld, new BlockPos(area.minX(), targetPos.getY(), area.minZ()), quarry);
        }
        if (flagMinX && flagMaxZ) {
            removeFluidAtPos(targetWorld, new BlockPos(area.minX(), targetPos.getY(), area.maxZ()), quarry);
        }
        if (flagMaxX && flagMinZ) {
            removeFluidAtPos(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), area.minZ()), quarry);
        }
        if (flagMaxX && flagMaxZ) {
            removeFluidAtPos(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), area.maxZ()), quarry);
        }
    }

    private static void removeFluidAtPos(ServerLevel world, BlockPos pos, TileQuarry quarry) {
        var state = world.getBlockState(pos);
        var fluidState = world.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            if (state.getBlock() instanceof BucketPickup fluidBlock) {
                quarry.useEnergy(PowerManager.getBreakBlockFluidEnergy(quarry), Reason.REMOVE_FLUID, true);
                var bucketItem = fluidBlock.pickupBlock(world, pos, state);
                quarry.storage.addFluid(bucketItem);
                if (world.getBlockState(pos).isAir() || (fluidBlock instanceof LiquidBlock && !fluidState.isSource())) {
                    world.setBlock(pos, Holder.BLOCK_FRAME.getDammingState(), Block.UPDATE_ALL);
                }
            } else if (state.getBlock() instanceof LiquidBlockContainer) {
                float hardness = state.getDestroySpeed(world, pos);
                quarry.useEnergy(PowerManager.getBreakEnergy(hardness, quarry), Reason.REMOVE_FLUID, true);
                var drops = Block.getDrops(state, world, pos, world.getBlockEntity(pos), null, quarry.getPickaxe());
                drops.forEach(quarry.storage::addItem);
                world.setBlock(pos, Holder.BLOCK_FRAME.getDammingState(), Block.UPDATE_ALL);
            }
        }
    }

    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
        setEnchantments(RESTRICTION.filterMap(enchantments).entrySet().stream()
            .map(EnchantmentLevel::new)
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
            .toList());
    }

    public void setEnchantments(List<EnchantmentLevel> enchantments) {
        this.enchantments = enchantments;
        this.cache.enchantments.expire();
        this.setMaxEnergy(getPowerConfig().maxEnergy() * (efficiencyLevel() + 1));
    }

    /**
     * TODO, Is this really needed?
     */
    public void setTileDataFromItem(@Nullable CompoundTag tileData) {
        if (tileData == null) {
            digMinY = level == null ? 0 : level.getMinBuildHeight();
            return;
        }
        if (tileData.contains("digMinY"))
            digMinY = tileData.getInt("digMinY");
        else
            digMinY = level == null ? 0 : level.getMinBuildHeight();
        // Module inventory is loaded in vanilla system.
    }

    public CompoundTag getTileDataForItem() {
        var tag = new CompoundTag();
        if (digMinY != 0) tag.putInt("digMinY", digMinY);
        return tag;
    }

    double headSpeed() {
        int l = efficiencyLevel();
        return headSpeed(l);
    }

    @VisibleForTesting
    static double headSpeed(int efficiency) {
        if (efficiency >= 4) {
            return Math.pow(2, efficiency - 4);
        } else {
            // 4th root of 8.
            return Math.pow(1.681792830507429, efficiency) / 8;
        }
    }

    void updateModules() {
        // Blocks
        Set<QuarryModule> blockModules = level != null
            ? QuarryModuleProvider.Block.getModulesInWorld(level, getBlockPos())
            : Collections.emptySet();

        // Module Inventory
        Set<QuarryModule> itemModules = Set.copyOf(moduleInventory.getModules());
        this.modules = Sets.union(blockModules, itemModules);
    }

    BlockState getReplacementState() {
        return cache.replaceState.getValue(level);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canBreak(Level targetWorld, BlockPos targetPos, BlockState state) {
        if (target != null && target.alreadySkipped(targetPos)) return false;
        if (state.isAir()) return true;
        var unbreakable = state.getDestroySpeed(targetWorld, targetPos) < 0;
        if (unbreakable) {
            if (hasBedrockModule() && state.getBlock() == Blocks.BEDROCK) {
                var worldBottom = targetWorld.getMinBuildHeight();
                if (targetWorld.dimension().equals(Level.NETHER)) {
                    return (worldBottom < targetPos.getY() && targetPos.getY() < worldBottom + 5) || (122 < targetPos.getY() && targetPos.getY() < cache.netherTop.getValue(targetWorld));
                } else {
                    return worldBottom < targetPos.getY() && targetPos.getY() < worldBottom + 5;
                }
            } else {
                return false;
            }
        } else if (isFullFluidBlock(state)) {
            return hasPumpModule();
        } else {
            return getReplacementState() != state;
        }
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "%sArea:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, area),
            "%sTarget:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, target),
            "%sState:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, state),
            "%sRemoveBedrock:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, hasBedrockModule()),
            "%sDigMinY:%s %d".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, digMinY),
            "%sHead:%s (%f, %f, %f)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, headX, headY, headZ),
            "%sModules:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, modules),
            energyString()
        ).map(Component::literal).toList();
    }

    @Override
    public MachineStorage getStorage() {
        return this.storage;
    }

    @Override
    public List<EnchantmentLevel> getEnchantments() {
        return Collections.unmodifiableList(enchantments);
    }

    public void sync() {
        if (level != null && !level.isClientSide)
            PacketHandler.sendToClient(new ClientSyncMessage(this), level);
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (area != null) {
            var min = level != null ? level.getMinBuildHeight() : 0;
            return new AABB(area.minX(), min, area.minZ(), area.maxX(), area.maxY(), area.maxZ());
        } else {
            return new AABB(getBlockPos(), getBlockPos().offset(1, 1, 1));
        }
    }

    @Override
    public ModuleInventory getModuleInventory() {
        return moduleInventory;
    }

    @Override
    public Set<QuarryModule> getLoadedModules() {
        return modules;
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
                () -> TileQuarry.this.getReplacerModule().map(ReplacerModule::getState).orElse(Blocks.AIR.defaultBlockState()));
            netherTop = CacheEntry.supplierCache(100,
                QuarryPlus.config.common.netherTop::get);
            enchantments = CacheEntry.supplierCache(1000, () -> EnchantmentHolder.makeHolder(TileQuarry.this));
        }
    }

}
