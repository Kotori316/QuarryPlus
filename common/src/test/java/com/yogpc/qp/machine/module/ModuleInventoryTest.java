package com.yogpc.qp.machine.module;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModuleInventoryTest extends BeforeMC {
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
            super(new Properties());
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
        var item = new Module1(module);
        assertTrue(inv.canPlaceItem(0, new ItemStack(item)));
    }

    @ParameterizedTest
    @EnumSource(QuarryModule.Constant.class)
    void notAcceptDuplicatedModule(QuarryModule.Constant module) {
        var inv = new ModuleInventory(5, q -> true, m -> Set.of(module));
        var item = new Module1(module);
        assertFalse(inv.canPlaceItem(0, new ItemStack(item)));
    }

    @Test
    void acceptNotDuplicatedModule() {
        var inv = new ModuleInventory(5, q -> true, m -> Set.of(QuarryModule.Constant.PUMP));
        var item = new Module1(QuarryModule.Constant.DUMMY);
        assertTrue(inv.canPlaceItem(0, new ItemStack(item)));
    }

    @ParameterizedTest
    @EnumSource(QuarryModule.Constant.class)
    void staticFilter(QuarryModule.Constant module) {
        var inv = new ModuleInventory(5, q -> false, m -> Set.of());
        var item = new Module1(module);
        assertFalse(inv.canPlaceItem(0, new ItemStack(item)));
    }

    @Test
    void notAcceptSecond() {
        var inv = new ModuleInventory(5);
        var item = new Module1(QuarryModule.Constant.DUMMY);
        inv.setItem(0, new ItemStack(item));

        var item2 = new Module1(QuarryModule.Constant.DUMMY);
        assertFalse(inv.canPlaceItem(1, new ItemStack(item2)));
    }

    @ParameterizedTest
    @EnumSource(QuarryModule.Constant.class)
    void getModule(QuarryModule.Constant module) {
        var inv = new ModuleInventory(5);
        var item = new Module1(module);
        inv.setItem(0, new ItemStack(item));

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
        var item = new Module2(QuarryModule.Constant.DUMMY, false);
        inv.setItem(0, new ItemStack(item));

        assertTrue(inv.getModules().isEmpty());
    }
}
