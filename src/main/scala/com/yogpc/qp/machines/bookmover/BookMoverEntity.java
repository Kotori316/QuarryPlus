package com.yogpc.qp.machines.bookmover;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BookMoverEntity extends PowerTile implements Container, MenuProvider, CheckerLog {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    static final Set<EnchantmentCategory> CATEGORIES = Arrays.stream(EnchantmentCategory.values())
            .filter(t -> t.canEnchant(Items.DIAMOND_PICKAXE))
            .collect(Collectors.toSet());
    static final Set<Enchantment> VALID_ENCHANTMENTS = ForgeRegistries.ENCHANTMENTS.getValues().stream().filter(e -> CATEGORIES.contains(e.category)).collect(Collectors.toSet());

    public BookMoverEntity(BlockPos pos, BlockState state) {
        super(Holder.BOOK_MOVER_TYPE, pos, state);
    }

    @Override
    public void saveNbtData(CompoundTag nbt) {
        ContainerHelper.saveAllItems(nbt, inventory);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        ContainerHelper.loadAllItems(nbt, inventory);
    }

    void workInTick() {
        if (!hasEnoughEnergy() || !isWorking()) return;
        if (getEnergy() >= getMaxEnergy()) {
            if (!canPlaceItem(0, inventory.get(0)) || !canPlaceItem(1, inventory.get(1))) return;
            // The enchantments which Enchantment Book in slot 1 has.
            var enchantments = EnchantmentHelper.getEnchantments(inventory.get(1));
            enchantments.entrySet().stream()
                    .filter(e ->
                            VALID_ENCHANTMENTS.contains(e.getKey()) &&
                                    EnchantmentHelper.getEnchantments(inventory.get(0)).keySet().stream().allMatch(e2 -> e2 == e.getKey() || e2.isCompatibleWith(e.getKey())) &&
                                    inventory.get(0).getEnchantmentLevel(e.getKey()) < e.getValue())
                    .findFirst()
                    .ifPresent(e -> {
                        var copy = inventory.get(0).copy();
                        removeEnchantment(e.getKey(), copy);
                        copy.enchant(e.getKey(), e.getValue());
                        if (enchantments.size() == 1) {
                            // Replace Enchantment Book to Book
                            setItem(1, new ItemStack(Items.BOOK));
                        } else {
                            // Remove Enchantment
                            removeEnchantment(e.getKey(), inventory.get(1));
                        }
                        setItem(0, ItemStack.EMPTY);
                        setItem(2, copy);
                        useEnergy(getMaxEnergy(), Reason.BOOK_MOVER, false);
                    });
        }
    }

    @Override
    protected long getMaxReceive() {
        if (isWorking())
            return getMaxEnergy() / 300;
        else
            return 0;
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(inventory, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(inventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        inventory.set(index, stack);
        setChanged();
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return switch (index) {
            case 0 -> stack.getItem() instanceof EnchantableItem;
            case 1 -> stack.getItem() == Items.ENCHANTED_BOOK;
            default -> false;
        };
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this, player);
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
                energyString()
        ).map(Component::literal).toList();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new BookMoverMenu(id, player, getBlockPos());
    }

    boolean isWorking() {
        return !inventory.get(0).isEmpty() && !inventory.get(1).isEmpty();
    }

    @VisibleForTesting
    static void removeEnchantment(Enchantment enchantment, ItemStack stack) {
        ListTag list;
        String tagName;
        if (stack.is(Items.ENCHANTED_BOOK)) {
            list = EnchantedBookItem.getEnchantments(stack);
            tagName = EnchantedBookItem.TAG_STORED_ENCHANTMENTS;
        } else {
            list = stack.getEnchantmentTags();
            tagName = ItemStack.TAG_ENCH;
        }
        stack.removeTagKey(tagName);
        var newList = list.stream()
                .mapMulti(MapMulti.cast(CompoundTag.class))
                .filter(t -> !Objects.equals(EnchantmentHelper.getEnchantmentId(t), ForgeRegistries.ENCHANTMENTS.getKey(enchantment)))
                .collect(Collectors.toCollection(ListTag::new));

        stack.addTagElement(tagName, newList);
    }
}
/*
 Test command.
 1 Fortune /give @p minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:fortune",lvl:6}]}
 2 Unbreaking /give @p minecraft:enchanted_book{StoredEnchantments:[{id:34,lvl:6}]}
 3 Fortune and Unbreaking /give @p minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:unbreaking",lvl:12}, {id:"minecraft:fortune",lvl:8}]}
 */
