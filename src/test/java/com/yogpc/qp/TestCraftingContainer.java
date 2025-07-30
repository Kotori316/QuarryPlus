package com.yogpc.qp;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestCraftingContainer implements CraftingContainer {
    private final int width;
    private final int height;
    private final List<ItemStack> items;

    public TestCraftingContainer(List<String> patterns, Map<Character, ItemStack> items) {
        this.width = patterns.stream().mapToInt(String::length).max().orElse(1);
        this.height = Math.max(1, patterns.size());
        var spacedMap = new HashMap<>(items);
        spacedMap.put(' ', ItemStack.EMPTY);
        this.items = patterns.stream().flatMap(s -> s.chars().mapToObj(i -> (char) i)).map(c -> spacedMap.getOrDefault(c, ItemStack.EMPTY)).toList();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return width * height;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return items.get(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }

    @Override
    public void clearContent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillStackedContents(StackedContents pContents) {
        this.items.forEach(pContents::accountStack);
    }
}
