package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class MoverEntity extends QpEntity implements ClientSync {
    final SimpleContainer inventory = new Inventory(2);

    public MoverEntity(BlockPos pos, BlockState blockState) {
        super(PlatformAccess.getAccess().registerObjects().getBlockEntityType((QpBlock) blockState.getBlock()).orElseThrow(),
            pos, blockState);
        inventory.addListener(container -> this.setChanged());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        inventory.fromItemList(input.listOrEmpty("inventory", ItemStack.CODEC));
        super.loadAdditional(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.storeAsItemList(output.list("inventory", ItemStack.CODEC));
    }

    @Override
    public void fromClientTag(ValueInput input) {
        movableEnchantments = input.listOrEmpty("enchantments", Enchantment.CODEC)
            .stream().toList();
    }

    @Override
    public ValueOutput toClientTag(ValueOutput output) {
        {
            var enchantmentList = output.list("enchantments", Enchantment.CODEC);
            movableEnchantments.forEach(enchantmentList::add);
        }
        return output;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        if (level != null) {
            Containers.dropContents(level, pos, inventory);
        }
    }

    private static class Inventory extends SimpleContainer {
        public Inventory(int size) {
            super(size);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> {
                    if (stack.is(Items.ENCHANTED_BOOK)) {
                        yield true;
                    }
                    if (!stack.isEnchanted()) {
                        yield false;
                    }
                    // TODO fix logic
                    if (stack.getItem() instanceof BowItem) {
                        yield true;
                    }
                    yield stack.has(DataComponents.TOOL) && stack.getMaxDamage() >= ToolMaterial.DIAMOND.durability();
                }
                case 1 -> stack.is(PlatformAccess.getAccess().registerObjects().quarryBlock().get().blockItem)
                    || stack.is(PlatformAccess.getAccess().registerObjects().advQuarryBlock().get().blockItem)
                    || stack.is(PlatformAccess.getAccess().registerObjects().advPumpBlock().get().blockItem);
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
        if (level != null && !level.isClientSide() && enabled) {
            var pre = movableEnchantments;
            // Update in server only.
            this.movableEnchantments = getMovable(inventory.getItem(0), inventory.getItem(1), e -> true);
            if (!pre.equals(movableEnchantments)) {
                syncToClient();
            }
        }
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
        var moved = moveEnchantment(enchantment, inventory.getItem(0), inventory.getItem(1), this::updateMovableEnchantments);
        inventory.setItem(0, moved.getLeft());
        inventory.setItem(1, moved.getRight());
    }

    static Pair<ItemStack, ItemStack> moveEnchantment(@Nullable Holder<Enchantment> enchantment, ItemStack from, ItemStack to, Runnable after) {
        return moveEnchantment(enchantment, from, to, null, after);
    }

    @VisibleForTesting
    static Pair<ItemStack, ItemStack> moveEnchantment(@Nullable Holder<Enchantment> enchantment, ItemStack from, ItemStack to, @Nullable Predicate<Holder<Enchantment>> predicate, Runnable after) {
        if (enchantment == null || from.isEmpty() || to.isEmpty()) return Pair.of(from, to);
        if (canMoveEnchantment(predicate, EnchantmentHelper.getEnchantmentsForCrafting(to), enchantment)) {
            var right = upLevel(enchantment, to);
            var left = downLevel(enchantment, from);
            after.run();
            return Pair.of(left, right);
        }
        return Pair.of(from, to);
    }

    @VisibleForTesting
    static ItemStack downLevel(Holder<Enchantment> enchantment, ItemStack stack) {
        EnchantmentHelper.updateEnchantments(stack, mutable ->
            mutable.set(enchantment, mutable.getLevel(enchantment) - 1)
        );
        if (stack.is(Items.ENCHANTED_BOOK) && EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty()) {
            // Remove empty enchanted book
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @VisibleForTesting
    static ItemStack upLevel(Holder<Enchantment> enchantment, ItemStack stack) {
        EnchantmentHelper.updateEnchantments(stack, mutable ->
            mutable.set(enchantment, mutable.getLevel(enchantment) + 1)
        );
        return stack;
    }
}
