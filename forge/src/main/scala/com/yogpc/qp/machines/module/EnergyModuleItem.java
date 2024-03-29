package com.yogpc.qp.machines.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPItem;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class EnergyModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "energy_module");
    private final int energy;

    public EnergyModuleItem(int energy, String path) {
        super(new ResourceLocation(QuarryPlus.modID, path), new Properties());
        this.energy = energy;
    }

    @Override
    // Overload for test.
    public EnergyModule getModule(ItemStack stack) {
        return getEnergyModule(this.energy, stack.getCount());
    }

    @NotNull
    static EnergyModule getEnergyModule(int itemEnergy, int stackCount) {
        if (itemEnergy > 0) {
            try {
                var energy = Math.multiplyExact(itemEnergy, stackCount);
                return new EnergyModule(energy);
            } catch (ArithmeticException ignore) {
                return new EnergyModule(Integer.MAX_VALUE);
            }
        } else {
            // I shouldn't extract energy as this is energy supplier module. So set to 0.
            return new EnergyModule(0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        list.add(Component.literal("Can be stacked to increase power."));
    }

    public static <T extends PowerTile & ModuleInventory.HasModuleInventory> BlockEntityTicker<T> energyModuleTicker() {
        return (w, p, s, blockEntity) ->
            blockEntity.getLoadedModules().stream()
                .mapMulti(MapMulti.cast(EnergyModule.class))
                .max(Comparator.comparingInt(EnergyModule::energy))
                .ifPresent(e -> blockEntity.addEnergy(e.energy * PowerTile.ONE_FE, false));
    }

    public record EnergyModule(int energy) implements QuarryModule {

        @Override
        public ResourceLocation moduleId() {
            return LOCATION;
        }
    }
}
