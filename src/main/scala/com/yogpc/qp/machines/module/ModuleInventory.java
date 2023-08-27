package com.yogpc.qp.machines.module;

import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ModuleInventory extends SimpleContainer implements INBTSerializable<CompoundTag> {
    private final Consumer<ModuleInventory> onUpdate;
    private final Predicate<QuarryModule> filter;
    private final HasModuleInventory holder;

    public ModuleInventory(int slot, Consumer<ModuleInventory> onUpdate, Predicate<QuarryModule> filter, HasModuleInventory holder) {
        super(slot);
        this.onUpdate = onUpdate;
        this.filter = filter;
        this.holder = holder;
    }

    public ModuleInventory(int slot, Runnable onUpdate, Predicate<QuarryModule> filter, HasModuleInventory holder) {
        this(slot, t -> onUpdate.run(), filter, holder);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (stack.getItem() instanceof QuarryModuleProvider.Item provider) {
            if (ItemStack.isSame(getItem(index), stack)) return true;
            var given = holder.getLoadedModules().stream().map(QuarryModule::moduleId).collect(Collectors.toSet());
            var toAdd = provider.getModule(stack);
            return filter.test(toAdd) && !given.contains(toAdd.moduleId());
        } else {
            return false;
        }
    }

    public List<QuarryModule> getModules() {
        return getModules(IntStream.range(0, getContainerSize()).mapToObj(this::getItem));
    }

    @VisibleForTesting
    static List<QuarryModule> getModules(Stream<ItemStack> stream) {
        return stream
            .filter(s -> s.getItem() instanceof QuarryModuleProvider.Item)
            .map(s -> ((QuarryModuleProvider.Item) s.getItem()).getModule(s))
            .toList();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onUpdate.accept(this);
        if (holder instanceof BlockEntity entity) {
            entity.setChanged();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putInt("slot", getContainerSize());
        tag.put("inventory", createTag());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        var list = nbt.getList("inventory", Tag.TAG_COMPOUND);
        fromTag(list);
    }

    @Override
    public boolean stillValid(Player player) {
        if (holder instanceof PowerTile powerTile)
            return PowerTile.stillValid(powerTile, player);
        return super.stillValid(player);
    }

    public static List<QuarryModule> loadModulesFromTag(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return Collections.emptyList();
        return getModules(tag.getList("inventory", Tag.TAG_COMPOUND).stream()
            .mapMulti(MapMulti.cast(CompoundTag.class))
            .map(ItemStack::of));
    }

    public interface HasModuleInventory {
        ModuleInventory getModuleInventory();

        Set<QuarryModule> getLoadedModules();

        default boolean hasPumpModule() {
            return getLoadedModules().contains(QuarryModule.Constant.PUMP);
        }

        default boolean hasBedrockModule() {
            return getLoadedModules().contains(QuarryModule.Constant.BEDROCK);
        }

        default boolean hasFillerModule() {
            return getLoadedModules().contains(QuarryModule.Constant.FILLER);
        }

        default Optional<ExpModule> getExpModule() {
            return getLoadedModules().stream().mapMulti(MapMulti.cast(ExpModule.class)).findFirst();
        }

        default Optional<ReplacerModule> getReplacerModule() {
            return getLoadedModules().stream().mapMulti(MapMulti.cast(ReplacerModule.class)).findFirst();
        }

        default int getRepeatWorkCount() {
            return getLoadedModules().stream().mapMulti(MapMulti.cast(RepeatTickModuleItem.RepeatTickModule.class))
                .mapToInt(RepeatTickModuleItem.RepeatTickModule::stackSize).sum() + 1;
        }
    }
}
