package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.QpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class MoverEntity extends BlockEntity {
    final SimpleContainer inventory = new Inventory(2);

    public MoverEntity(BlockPos pos, BlockState blockState) {
        super(PlatformAccess.getAccess().registerObjects().getBlockEntityType((QpBlock) blockState.getBlock()).orElseThrow(),
            pos, blockState);
        inventory.addListener(container -> this.setChanged());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        inventory.fromTag(tag.getList("inventory", Tag.TAG_COMPOUND), registries);
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.createTag(registries));
    }

    private static class Inventory extends SimpleContainer {
        public Inventory(int size) {
            super(size);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> {
                    if (!stack.isEnchanted()) {
                        yield false;
                    }
                    yield switch (stack.getItem()) {
                        case TieredItem tieredItem -> tieredItem.getTier().getUses() >= Tiers.DIAMOND.getUses();
                        case BowItem ignore -> true;
                        default -> false;
                    };
                }
                case 1 -> stack.is(PlatformAccess.getAccess().registerObjects().quarryBlock().get().blockItem);
                default -> false;
            };
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateMovableEnchantments();
    }

    void updateMovableEnchantments() {
        this.movableEnchantments = getMovable(inventory.getItem(0), inventory.getItem(1), e -> true);
    }

    List<Holder<Enchantment>> movableEnchantments = List.of();

    @VisibleForTesting
    static List<Holder<Enchantment>> getMovable(ItemStack from, ItemStack to, Predicate<Holder<Enchantment>> predicate) {
        if (from.isEmpty() || to.isEmpty()) {
            return List.of();
        }
        var given = EnchantmentHelper.getEnchantmentsForCrafting(to);
        return EnchantmentHelper.getEnchantmentsForCrafting(from).keySet().stream()
            .filter(e -> canMoveEnchantment(predicate, given, e))
            .sorted(Comparator.comparing(Holder::getRegisteredName))
            .toList();
    }

    @VisibleForTesting
    static boolean canMoveEnchantment(@Nullable Predicate<Holder<Enchantment>> predicate, ItemEnchantments given, Holder<Enchantment> toMove) {
        return
            (predicate == null || predicate.test(toMove)) &&
                given.getLevel(toMove) < toMove.value().getMaxLevel() &&
                given.keySet().stream().filter(Predicate.isEqual(toMove).negate()).allMatch(e -> Enchantment.areCompatible(e, toMove));
    }

    void moveEnchant(Holder<Enchantment> enchantment) {
        moveEnchantment(enchantment, inventory.getItem(0), inventory.getItem(1), this::updateMovableEnchantments);
    }

    static void moveEnchantment(@Nullable Holder<Enchantment> enchantment, ItemStack from, ItemStack to, Runnable after) {
        moveEnchantment(enchantment, from, to, null, after);
    }

    @VisibleForTesting
    static void moveEnchantment(@Nullable Holder<Enchantment> enchantment, ItemStack from, ItemStack to, @Nullable Predicate<Holder<Enchantment>> predicate, Runnable after) {
        if (enchantment == null || from.isEmpty() || to.isEmpty()) return;
        if (canMoveEnchantment(predicate, EnchantmentHelper.getEnchantmentsForCrafting(to), enchantment)) {
            upLevel(enchantment, to);
            downLevel(enchantment, from);
            after.run();
        }
    }

    @VisibleForTesting
    static void downLevel(Holder<Enchantment> enchantment, ItemStack stack) {
        EnchantmentHelper.updateEnchantments(stack, mutable ->
            mutable.set(enchantment, mutable.getLevel(enchantment) - 1)
        );
    }

    @VisibleForTesting
    static void upLevel(Holder<Enchantment> enchantment, ItemStack stack) {
        EnchantmentHelper.updateEnchantments(stack, mutable ->
            mutable.set(enchantment, mutable.getLevel(enchantment) + 1)
        );
    }
}
