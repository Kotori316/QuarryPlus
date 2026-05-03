package com.yogpc.qp.machine.module;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ModuleInventoryTest extends BeforeMC {
    private final Runnable empty = () -> {
    };

    private static final Map<QuarryModule.Constant, Module1> MODULE1_MAP = new EnumMap<>(QuarryModule.Constant.class);
    private static Module2 disabledModule2;

    @SuppressWarnings("deprecation")
    @BeforeAll
    static void bindItemComponents() {
        for (var c : QuarryModule.Constant.values()) {
            var item = new Module1(c);
            item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
            MODULE1_MAP.put(c, item);
        }
        disabledModule2 = new Module2(QuarryModule.Constant.DUMMY, false);
        disabledModule2.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
        Items.APPLE.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
    }

    @Test
    void instance() {
        var inv = assertDoesNotThrow(() -> new ModuleInventory(5));
        assertNotNull(inv);
    }

    @Test
    void acceptApple() {
        var inv = new ModuleInventory(5);
        assertFalse(inv.canPlaceItem(0, Items.APPLE.getDefaultInstance()));
    }

    private static final class Module1 extends Item implements QuarryModuleProvider.Item {
        private final QuarryModule module;

        Module1(QuarryModule module) {
            super(new Properties().setId(QuarryPlus.itemKey("module1")));
            this.module = module;
        }

        @Override
        public QuarryModule getModule(@NotNull ItemStack stack) {
            return module;
        }
    }

    @ParameterizedTest
    @EnumSource(QuarryModule.Constant.class)
    void acceptModuleItem(QuarryModule.Constant module) {
        var inv = new ModuleInventory(5);
        assertTrue(inv.canPlaceItem(0, new ItemStack(MODULE1_MAP.get(module))));
    }

    @ParameterizedTest
    @EnumSource(QuarryModule.Constant.class)
    void notAcceptDuplicatedModule(QuarryModule.Constant module) {
        var inv = new ModuleInventory(5, q -> true, m -> Set.of(module), empty);
        assertFalse(inv.canPlaceItem(0, new ItemStack(MODULE1_MAP.get(module))));
    }

    @Test
    void acceptNotDuplicatedModule() {
        var inv = new ModuleInventory(5, q -> true, m -> Set.of(QuarryModule.Constant.PUMP), empty);
        assertTrue(inv.canPlaceItem(0, new ItemStack(MODULE1_MAP.get(QuarryModule.Constant.DUMMY))));
    }

    @ParameterizedTest
    @EnumSource(QuarryModule.Constant.class)
    void staticFilter(QuarryModule.Constant module) {
        var inv = new ModuleInventory(5, q -> false, m -> Set.of(), empty);
        assertFalse(inv.canPlaceItem(0, new ItemStack(MODULE1_MAP.get(module))));
    }

    @Test
    void notAcceptSecond() {
        var inv = new ModuleInventory(5);
        var item = MODULE1_MAP.get(QuarryModule.Constant.DUMMY);
        inv.setItem(0, new ItemStack(item));
        assertFalse(inv.canPlaceItem(1, new ItemStack(item)));
    }

    @ParameterizedTest
    @EnumSource(QuarryModule.Constant.class)
    void getModule(QuarryModule.Constant module) {
        var inv = new ModuleInventory(5);
        inv.setItem(0, new ItemStack(MODULE1_MAP.get(module)));
        assertEquals(Set.of(module), inv.getModules());
    }

    private static final class Module2 extends QpItem implements QuarryModuleProvider.Item {
        private final QuarryModule module;
        private final boolean isEnabled;

        Module2(QuarryModule module, boolean isEnabled) {
            super(new Properties(), "module2");
            this.module = module;
            this.isEnabled = isEnabled;
        }

        @Override
        public QuarryModule getModule(@NotNull ItemStack stack) {
            return module;
        }

        @Override
        public boolean isEnabled() {
            return isEnabled;
        }
    }

    @Test
    void disabledItem() {
        var inv = new ModuleInventory(5);
        inv.setItem(0, new ItemStack(disabledModule2));
        assertTrue(inv.getModules().isEmpty());
    }

    @Test
    void onUpdate() {
        var item = MODULE1_MAP.get(QuarryModule.Constant.DUMMY);
        AtomicInteger integer = new AtomicInteger(0);
        var inv = new ModuleInventory(5, q -> true, m -> Set.of(), integer::getAndIncrement);
        inv.setItem(0, new ItemStack(item));
        assertEquals(1, integer.get());
        inv.setItem(1, new ItemStack(item));
        assertEquals(2, integer.get());
    }
}
