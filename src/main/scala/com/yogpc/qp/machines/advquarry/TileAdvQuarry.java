package com.yogpc.qp.machines.advquarry;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QuarryFakePlayer;
import com.yogpc.qp.machines.module.ModuleInventory;
import com.yogpc.qp.machines.module.QuarryModule;
import com.yogpc.qp.machines.module.QuarryModuleProvider;
import com.yogpc.qp.machines.module.ReplacerModule;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.utils.CacheEntry;
import com.yogpc.qp.utils.MapMulti;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class TileAdvQuarry extends PowerTile implements
    CheckerLog, ModuleInventory.HasModuleInventory, MachineStorage.HasStorage,
    EnchantmentLevel.HasEnchantments, ClientSync, MenuProvider {

    // Inventory
    private final ModuleInventory moduleInventory = new ModuleInventory(5, this::updateModule, TileAdvQuarry::moduleFilter, this);
    private Set<QuarryModule> modules = Set.of();
    private boolean isBlockModuleLoaded = false;
    private final MachineStorage storage = new MachineStorage();

    // Work
    private final QuarryCache cache = new QuarryCache();
    private final ItemConverter itemConverter = ItemConverter.defaultConverter();
    public int digMinY;
    @Nullable
    Area area = null;
    private List<EnchantmentLevel> enchantments = List.of();
    AdvQuarryAction action = AdvQuarryAction.Waiting.WAITING;

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
            "%sEnergy:%s %f FE (%d)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getEnergy())
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
        if (area != null)
            return new AABB(area.minX(), 0, area.minZ(), area.maxX(), area.maxY(), area.maxZ());
        else
            return new AABB(getBlockPos(), getBlockPos().offset(1, 1, 1));
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("moduleInventory", moduleInventory.serializeNBT());
        nbt.put("storage", storage.toNbt());
        toClientTag(nbt);
        nbt.putInt("digMinY", digMinY);
        return super.save(nbt);
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
        action = AdvQuarryAction.fromNbt(nbt.getCompound("action"), this);
    }

    /**
     * Set enchantment of this machine.
     *
     * @param enchantments should be sorted with {@link EnchantmentLevel#QUARRY_ENCHANTMENT_COMPARATOR}
     */
    public void setEnchantments(List<EnchantmentLevel> enchantments) {
        this.enchantments = enchantments;
        maxEnergy = 10000 * ONE_FE * (efficiencyLevel() + 1);
    }

    @Nullable
    public Area getArea() {
        return area;
    }

    public AdvQuarryAction getAction() {
        return action;
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
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AdvQuarryMenu createMenu(int id, Inventory p, Player player) {
        return new AdvQuarryMenu(id, player, getBlockPos());
    }

    public ServerLevel getTargetWorld() {
        return (ServerLevel) this.level;
    }

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
        // if (hasPumpModule()) checkEdgeFluid(targetPos, targetWorld, this);

        // Break block
        var hardness = state.getDestroySpeed(targetWorld, targetPos);
        if (requireEnergy && !useEnergy(PowerManager.getBreakEnergy(hardness, this), Reason.BREAK_BLOCK, false)) {
            return BreakResult.NOT_ENOUGH_ENERGY;
        }
        // Get drops
        var drops = Block.getDrops(state, targetWorld, targetPos, targetWorld.getBlockEntity(targetPos), null, pickaxe);
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

        return BreakResult.SUCCESS;
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
