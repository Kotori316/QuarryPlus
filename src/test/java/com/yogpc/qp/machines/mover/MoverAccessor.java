package com.yogpc.qp.machines.mover;

import java.util.stream.Stream;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class MoverAccessor {
    public static Slot createMoverSlot(Container container, int index, int x, int y) {
        return new SlotMover(container, index, x, y);
    }

    public static Stream<Item> diamondTools() {
        return Stream.of(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_SWORD);
    }

    public static Stream<Item> netheriteTools() {
        return Stream.of(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_SWORD);
    }

    public static Stream<Item> ironTools() {
        return Stream.of(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_CHESTPLATE, Items.IRON_SWORD);
    }
}
