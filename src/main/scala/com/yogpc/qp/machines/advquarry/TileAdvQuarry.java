package com.yogpc.qp.machines.advquarry;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.module.ModuleInventory;
import com.yogpc.qp.machines.module.QuarryModule;
import com.yogpc.qp.machines.module.QuarryModuleProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileAdvQuarry extends PowerTile implements
    CheckerLog, ModuleInventory.HasModuleInventory, MachineStorage.HasStorage {

    // Inventory
    private final ModuleInventory moduleInventory = new ModuleInventory(5, this::updateModule, TileAdvQuarry::moduleFilter, this);
    private Set<QuarryModule> modules = Set.of();
    private boolean isBlockModuleLoaded = false;
    private final MachineStorage storage = new MachineStorage();

    // Work
    public int digMinY;

    public TileAdvQuarry(BlockPos pos, BlockState state) {
        super(Holder.ADV_QUARRY_TYPE, pos, state);
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
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
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("moduleInventory", moduleInventory.serializeNBT());
        nbt.put("storage", storage.toNbt());
        nbt.putInt("digMinY", digMinY);
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        moduleInventory.deserializeNBT(nbt.getCompound("moduleInventory"));
        storage.readNbt(nbt.getCompound("storage"));
        digMinY = nbt.getInt("digMinY");
        isBlockModuleLoaded = false;
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
}
