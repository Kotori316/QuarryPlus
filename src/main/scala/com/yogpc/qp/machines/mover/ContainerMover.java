package com.yogpc.qp.machines.mover;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.EnchantmentLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class ContainerMover extends AbstractContainerMenu {
    final Container craftMatrix = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            updateEnchantmentList();
        }
    };
    private final Level worldObj;
    List<Enchantment> movable = Collections.emptyList();
    @Nullable
    Enchantment selected = null;
    final BlockPos pos;

    public ContainerMover(int id, Player player, BlockPos pos) {
        super(Holder.MOVER_MENU_TYPE, id);
        Inventory inv = player.getInventory();
        this.worldObj = player.level();
        this.pos = pos;
        int row;
        int col;
        for (col = 0; col < 2; ++col)
            addSlot(new SlotMover(this.craftMatrix, col, 8 + col * 144, 40));

        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 104 + row * 18));

        for (col = 0; col < 9; ++col)
            addSlot(new Slot(inv, col, 8 + col * 18, 162));
    }

    @Override
    public void removed(final Player playerIn) {
        super.removed(playerIn);
        if (!worldObj.isClientSide) {
            IntStream.range(0, craftMatrix.getContainerSize())
                    .mapToObj(craftMatrix::removeItemNoUpdate)
                    .filter(Predicate.not(ItemStack::isEmpty))
                    .forEach(playerIn.getInventory()::placeItemBackInInventory);
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.worldObj.getBlockState(pos).is(Holder.BLOCK_MOVER) && playerIn.distanceToSqr(Vec3.atCenterOf(pos)) <= 64.0D;
    }

    void updateEnchantmentList() {
        var from = craftMatrix.getItem(0);
        var to = craftMatrix.getItem(1);
        if (from.isEmpty() ||
                to.isEmpty() ||
                from.getEnchantmentTags().isEmpty() ||
                !(to.getItem() instanceof EnchantableItem)) {
            movable = Collections.emptyList();
            selected = null;
        } else {
            var newMovable = getMovable(from, to, (EnchantableItem) to.getItem());
            if (newMovable.isEmpty()) {
                selected = null;
            } else {
                if (!newMovable.contains(selected))
                    selected = newMovable.get(0);
            }
            movable = newMovable;
        }
    }

    @VisibleForTesting
    static List<Enchantment> getMovable(ItemStack from, ItemStack to, Predicate<Enchantment> predicate) {
        var given = EnchantmentHelper.getEnchantments(to);
        return EnchantmentLevel.fromItem(from).stream()
                .map(EnchantmentLevel::enchantment)
                .filter(e -> canMoveEnchantment(predicate, given, e))
                .toList();
    }

    @VisibleForTesting
    static boolean canMoveEnchantment(@Nullable Predicate<Enchantment> predicate, Map<Enchantment, Integer> given, Enchantment toMove) {
        return
                (predicate == null || predicate.test(toMove)) &&
                        given.getOrDefault(toMove, 0) < toMove.getMaxLevel() &&
                        given.keySet().stream().filter(Predicate.isEqual(toMove).negate()).allMatch(toMove::isCompatibleWith);
    }

    public Optional<Enchantment> getEnchantment() {
        return Optional.ofNullable(selected);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.getSlot(index);
        if (slot.hasItem()) {
            final ItemStack remain = slot.getItem();
            src = remain.copy();
            if (index < 2) {
                if (!moveItemStackTo(remain, 2, 38, true))
                    return ItemStack.EMPTY;
            } else {
                Slot toSlot;
                final ItemStack put = ItemHandlerHelper.copyStackWithSize(remain, 1);
                boolean changed = false;
                toSlot = this.getSlot(0);
                if (/*!changed(true) &&*/ toSlot.mayPlace(remain) && toSlot.getItem().isEmpty()) {
                    toSlot.set(put);
                    remain.shrink(1);
                    changed = true;
                }
                toSlot = this.getSlot(1);
                if (!changed && toSlot.mayPlace(remain) && toSlot.getItem().isEmpty()) {
                    toSlot.set(put);
                    remain.shrink(1);
                    changed = true;
                }
                if (!changed)
                    return ItemStack.EMPTY;
            }
            if (remain.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
            if (remain.getCount() == src.getCount())
                return ItemStack.EMPTY;
            slot.onTake(playerIn, remain);
        }
        return src;
    }

    public void moveEnchant(Enchantment enchantment) {
        var from = craftMatrix.getItem(0);
        var to = craftMatrix.getItem(1);
        moveEnchantment(enchantment, from, to, this::updateEnchantmentList);
    }

    static void moveEnchantment(@Nullable Enchantment enchantment, ItemStack from, ItemStack to, Runnable after) {
        moveEnchantment(enchantment, from, to, to.getItem() instanceof EnchantableItem item ? item : null, after);
    }

    @VisibleForTesting
    static void moveEnchantment(@Nullable Enchantment enchantment, ItemStack from, ItemStack to, @Nullable Predicate<Enchantment> predicate, Runnable after) {
        if (enchantment == null || from.isEmpty() || to.isEmpty()) return;
        if (canMoveEnchantment(predicate, EnchantmentHelper.getEnchantments(to), enchantment)) {
            upLevel(enchantment, to);
            downLevel(enchantment, from);
            after.run();
        }
    }

    @VisibleForTesting
    static void downLevel(Enchantment enchantment, ItemStack stack) {
        var list = stack.getEnchantmentTags();
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                var nbt = list.getCompound(i);
                if (ForgeRegistries.ENCHANTMENTS.getValue(EnchantmentHelper.getEnchantmentId(nbt)) == enchantment) {
                    int l = EnchantmentHelper.getEnchantmentLevel(nbt);
                    if (l == 1) {
                        list.remove(i);
                    } else {
                        EnchantmentHelper.setEnchantmentLevel(nbt, l - 1);
                    }
                    break;
                }
            }
            if (list.isEmpty()) {
                stack.removeTagKey("Enchantments");
            }
        }
    }

    @VisibleForTesting
    static void upLevel(Enchantment enchantment, ItemStack stack) {
        var level = stack.getEnchantmentLevel(enchantment);
        if (level == 0) {
            stack.enchant(enchantment, 1);
        } else {
            var list = stack.getEnchantmentTags();
            for (int i = 0; i < list.size(); i++) {
                var nbt = list.getCompound(i);
                if (ForgeRegistries.ENCHANTMENTS.getValue(EnchantmentHelper.getEnchantmentId(nbt)) == enchantment) {
                    EnchantmentHelper.setEnchantmentLevel(nbt, level + 1);
                    break;
                }
            }
        }
    }
}

class SlotMover extends Slot {
    public SlotMover(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        switch (this.getSlotIndex()) {
            case 0:
                if (stack.getEnchantmentTags().isEmpty())
                    return false;
                if (stack.getItem() instanceof TieredItem tieredItem) {
                    return !TierSortingRegistry.getTiersLowerThan(Tiers.DIAMOND).contains(tieredItem.getTier());
                } else if (stack.getItem() instanceof ArmorItem armorItem) {
                    if (armorItem.getMaterial() instanceof ArmorMaterials material) {
                        return material.ordinal() >= ArmorMaterials.DIAMOND.ordinal();
                    } else {
                        return false; // Not vanilla armor.
                    }
                } else {
                    return stack.getItem() instanceof BowItem;
                }
            case 1:
                return stack.getItem() instanceof EnchantableItem;
        }
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
