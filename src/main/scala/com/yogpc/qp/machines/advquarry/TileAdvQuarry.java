package com.yogpc.qp.machines.advquarry;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.module.ModuleInventory;
import com.yogpc.qp.machines.module.QuarryModule;
import com.yogpc.qp.machines.module.QuarryModuleProvider;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.utils.MapMulti;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
        action = AdvQuarryAction.fromNbt(nbt.getCompound("action"));
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
}
