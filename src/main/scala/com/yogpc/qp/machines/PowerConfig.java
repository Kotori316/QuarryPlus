package com.yogpc.qp.machines;

import com.yogpc.qp.QuarryPlus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

public interface PowerConfig {
    PowerConfig DEFAULT = new DefaultConfig();

    static PowerConfig getMachineConfig(String machineName) {
        if (QuarryPlus.config == null) return DEFAULT;
        else if (!QuarryPlus.config.powerMap.has(machineName)) return DEFAULT;
        else return new MachinePowerConfig(machineName);
    }

    static Stream<Method> getAllMethods() {
        return Arrays.stream(PowerConfig.class.getMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> Character.isLowerCase(m.getName().charAt(0)))
                .filter(m -> m.getReturnType() == Long.TYPE || m.getReturnType() == Double.TYPE);
    }

    long maxEnergy();

    long makeFrame();

    long breakBlockBase();

    long breakBlockFluid();

    long moveHeadBase();

    long expCollect();

    double efficiencyCoefficient();

    double breakEfficiencyCoefficient();

    double breakFortuneCoefficient();

    double breakSilktouchCoefficient();

    interface Provider {
        @NotNull
        PowerConfig getPowerConfig();
    }
}

class DefaultConfig implements PowerConfig {
    // Base Energy
    private static final long MAKE_FRAME = PowerTile.ONE_FE * 15;
    private static final long BREAK_BLOCK_BASE = PowerTile.ONE_FE * 25;
    private static final long BREAK_BLOCK_FLUID = BREAK_BLOCK_BASE * 5;
    private static final long MOVE_HEAD_BASE = PowerTile.ONE_FE;
    private static final long EXP_COLLECT = BREAK_BLOCK_BASE / 10;

    // Coefficient
    private static final double FIFTH_ROOT_OF_10 = 1.5848931924611136;
    private static final double FIFTH_ROOT_OF_5 = 1.379729661461215;
    private static final double FORTUNE_COEFFICIENT = 1.5874010519681996; // 3rd root of 4
    private static final double SILKTOUCH_COEFFICIENT = 4;

    // @formatter:off
    @Override public long maxEnergy() {return 1000 * PowerTile.ONE_FE;}
    @Override public long makeFrame() {return MAKE_FRAME;}
    @Override public long breakBlockBase() {return BREAK_BLOCK_BASE;}
    @Override public long breakBlockFluid() {return BREAK_BLOCK_FLUID;}
    @Override public long moveHeadBase() {return MOVE_HEAD_BASE;}
    @Override public long expCollect() {return EXP_COLLECT;}
    @Override public double efficiencyCoefficient() {return FIFTH_ROOT_OF_10;}
    @Override public double breakEfficiencyCoefficient() {return FIFTH_ROOT_OF_5;}
    @Override public double breakFortuneCoefficient() {return FORTUNE_COEFFICIENT;}
    @Override public double breakSilktouchCoefficient() {return SILKTOUCH_COEFFICIENT;}
    // @formatter:on
}

class MachinePowerConfig implements PowerConfig {
    private final String machineName;
    final double maxEnergy;
    final double makeFrame;
    final double breakBlockBase;
    final double breakBlockFluid;
    final double moveHeadBase;
    final double expCollect;
    final double efficiencyCoefficient;
    final double breakEfficiencyCoefficient;
    final double breakFortuneCoefficient;
    final double breakSilktouchCoefficient;

    MachinePowerConfig(String machineName) {
        this.machineName = machineName;
        var powerMap = QuarryPlus.config.powerMap;

        this.maxEnergy = powerMap.get(machineName, "maxEnergy").orElse((double) DEFAULT.maxEnergy() / PowerTile.ONE_FE);
        this.makeFrame = powerMap.get(machineName, "makeFrame").orElse((double) DEFAULT.makeFrame() / PowerTile.ONE_FE);
        this.breakBlockBase = powerMap.get(machineName, "breakBlockBase").orElse((double) DEFAULT.breakBlockBase() / PowerTile.ONE_FE);
        this.breakBlockFluid = powerMap.get(machineName, "breakBlockFluid").orElse((double) DEFAULT.breakBlockFluid() / PowerTile.ONE_FE);
        this.moveHeadBase = powerMap.get(machineName, "moveHeadBase").orElse((double) DEFAULT.moveHeadBase() / PowerTile.ONE_FE);
        this.expCollect = powerMap.get(machineName, "expCollect").orElse((double) DEFAULT.expCollect() / PowerTile.ONE_FE);
        this.efficiencyCoefficient = powerMap.get(machineName, "efficiencyCoefficient").orElse(DEFAULT.efficiencyCoefficient());
        this.breakEfficiencyCoefficient = powerMap.get(machineName, "breakEfficiencyCoefficient").orElse(DEFAULT.breakEfficiencyCoefficient());
        this.breakFortuneCoefficient = powerMap.get(machineName, "breakFortuneCoefficient").orElse(DEFAULT.breakFortuneCoefficient());
        this.breakSilktouchCoefficient = powerMap.get(machineName, "breakSilktouchCoefficient").orElse(DEFAULT.breakSilktouchCoefficient());
    }

    @Override
    public String toString() {
        return "MachinePowerConfig{" +
                "machineName='" + machineName + '\'' +
                '}';
    }

    // @formatter:off
    @Override public long maxEnergy() {return (long) (maxEnergy * PowerTile.ONE_FE);}
    @Override public long makeFrame() {return (long) (makeFrame * PowerTile.ONE_FE);}
    @Override public long breakBlockBase() {return (long) (breakBlockBase * PowerTile.ONE_FE);}
    @Override public long breakBlockFluid() {return (long) (breakBlockFluid * PowerTile.ONE_FE);}
    @Override public long moveHeadBase() {return (long) (moveHeadBase * PowerTile.ONE_FE);}
    @Override public long expCollect() {return (long) (expCollect * PowerTile.ONE_FE);}
    @Override public double efficiencyCoefficient() {return efficiencyCoefficient;}
    @Override public double breakEfficiencyCoefficient() {return breakEfficiencyCoefficient;}
    @Override public double breakFortuneCoefficient() {return breakFortuneCoefficient;}
    @Override public double breakSilktouchCoefficient() {return breakSilktouchCoefficient;}
    // @formatter:on
}
