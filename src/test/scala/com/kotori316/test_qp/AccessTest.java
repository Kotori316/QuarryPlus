package com.kotori316.test_qp;

import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.base.Area;
import net.minecraft.util.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessTest extends InitMC {
    @Test
    void accessAreaClass() {
        String name = "com.yogpc.qp.machines.base.Area";
        checkClass(name);
    }

    @Test
    void accessDirectionClass() {
        checkClass("net.minecraft.util.Direction");
    }

    @Test
    void accessInvUtilClass() {
        String name = "com.yogpc.qp.compat.InvUtils";
        checkClass(name);
    }

    private static void checkClass(String name) {
        assertDoesNotThrow(() -> {
            Class<?> aClass = Class.forName(name);
            assertEquals(name, aClass.getName());
        }, String.format("Check class existence of %s", name));
    }

    @Test
    void getAreaInstance() {
        assertDoesNotThrow(() -> {
            Area area = Area.zeroArea();
            assertEquals(Area.zeroArea(), area);
        }, "Getting Area instance");
    }

    @Test
    void getDirectionInstance() {
        assertDoesNotThrow(() -> {
            Direction north = Direction.NORTH;
            assertEquals(north, Direction.SOUTH.getOpposite());
        }, "Getting Direction instance");
    }

    @Test
    void getInvUtilInstance() {
        assertDoesNotThrow(() -> {
            @SuppressWarnings("InstantiationOfUtilityClass")
            InvUtils utils = new InvUtils();
            assertEquals(InvUtils.class, utils.getClass());
        }, "Getting InvUtil instance");
    }
}
