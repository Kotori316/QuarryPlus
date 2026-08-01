package com.yogpc.qp.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Unit is FE, not microFE, so I use double in this class
 */
public record PowerMap(Quarry quarry, AdvQuarry advQuarry, AdvPump advPump) {
    public record Quarry(
        double maxEnergy,
        double makeFrame,
        double breakBlockBase,
        double breakBlockFluid,
        double moveHeadBase,
        double expCollect,
        double efficiencyCoefficient,
        double breakEfficiencyCoefficient,
        double breakFortuneCoefficient,
        double breakSilktouchCoefficient
    ) {
        public static final MapCodec<Quarry> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.DOUBLE.fieldOf("maxEnergy").forGetter(Quarry::maxEnergy),
            Codec.DOUBLE.fieldOf("makeFrame").forGetter(Quarry::makeFrame),
            Codec.DOUBLE.fieldOf("breakBlockBase").forGetter(Quarry::breakBlockBase),
            Codec.DOUBLE.fieldOf("breakBlockFluid").forGetter(Quarry::breakBlockFluid),
            Codec.DOUBLE.fieldOf("moveHeadBase").forGetter(Quarry::moveHeadBase),
            Codec.DOUBLE.fieldOf("expCollect").forGetter(Quarry::expCollect),
            Codec.DOUBLE.fieldOf("efficiencyCoefficient").forGetter(Quarry::efficiencyCoefficient),
            Codec.DOUBLE.fieldOf("breakEfficiencyCoefficient").forGetter(Quarry::breakEfficiencyCoefficient),
            Codec.DOUBLE.fieldOf("breakFortuneCoefficient").forGetter(Quarry::breakFortuneCoefficient),
            Codec.DOUBLE.fieldOf("breakSilktouchCoefficient").forGetter(Quarry::breakSilktouchCoefficient)
        ).apply(i, Quarry::new));

        public long getBreakEnergy(float hardness, int efficiency, int unbreaking, int fortune, boolean silktouch) {
            return PowerMap.getBreakEnergy(hardness, efficiency, unbreaking, fortune, silktouch, breakBlockBase, breakFortuneCoefficient, breakEfficiencyCoefficient, breakSilktouchCoefficient);
        }
    }

    public record AdvQuarry(
        double maxEnergy,
        double makeFrame,
        double breakBlockBase,
        double breakBlockFluid,
        double searchBase,
        double expCollect,
        double breakEfficiencyCoefficient,
        double breakFortuneCoefficient,
        double breakSilktouchCoefficient
    ) {
        public static final MapCodec<AdvQuarry> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.DOUBLE.fieldOf("maxEnergy").forGetter(AdvQuarry::maxEnergy),
            Codec.DOUBLE.fieldOf("makeFrame").forGetter(AdvQuarry::makeFrame),
            Codec.DOUBLE.fieldOf("breakBlockBase").forGetter(AdvQuarry::breakBlockBase),
            Codec.DOUBLE.fieldOf("breakBlockFluid").forGetter(AdvQuarry::breakBlockFluid),
            Codec.DOUBLE.fieldOf("searchBase").forGetter(AdvQuarry::searchBase),
            Codec.DOUBLE.fieldOf("expCollect").forGetter(AdvQuarry::expCollect),
            Codec.DOUBLE.fieldOf("breakEfficiencyCoefficient").forGetter(AdvQuarry::breakEfficiencyCoefficient),
            Codec.DOUBLE.fieldOf("breakFortuneCoefficient").forGetter(AdvQuarry::breakFortuneCoefficient),
            Codec.DOUBLE.fieldOf("breakSilktouchCoefficient").forGetter(AdvQuarry::breakSilktouchCoefficient)
        ).apply(i, AdvQuarry::new));

        public long getBreakEnergy(float hardness, int efficiency, int unbreaking, int fortune, boolean silktouch) {
            return PowerMap.getBreakEnergy(hardness, efficiency, unbreaking, fortune, silktouch, breakBlockBase, breakFortuneCoefficient, breakEfficiencyCoefficient, breakSilktouchCoefficient);
        }
    }

    /**
     * Unit is FE, not microFE, so I use double in this class
     */
    public record AdvPump(
        double maxEnergy,
        double fluidCapacity,
        double range,
        double baseEnergyLevel0,
        double baseEnergyLevel1,
        double baseEnergyLevel2,
        double baseEnergyLevel3
    ) {
        public static final MapCodec<AdvPump> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.DOUBLE.fieldOf("maxEnergy").forGetter(AdvPump::maxEnergy),
            Codec.DOUBLE.fieldOf("fluidCapacity").forGetter(AdvPump::fluidCapacity),
            Codec.DOUBLE.fieldOf("range").forGetter(AdvPump::range),
            Codec.DOUBLE.fieldOf("baseEnergyLevel0").forGetter(AdvPump::baseEnergyLevel0),
            Codec.DOUBLE.fieldOf("baseEnergyLevel1").forGetter(AdvPump::baseEnergyLevel1),
            Codec.DOUBLE.fieldOf("baseEnergyLevel2").forGetter(AdvPump::baseEnergyLevel2),
            Codec.DOUBLE.fieldOf("baseEnergyLevel3").forGetter(AdvPump::baseEnergyLevel3)
        ).apply(i, AdvPump::new));

        /**
         * @param unbreaking Level of Unbreaking enchantment. Clamped to [0, 3].
         * @return Energy in FE required to drain one fluid source block.
         */
        public long baseEnergyFor(int unbreaking) {
            var table = new double[]{baseEnergyLevel0, baseEnergyLevel1, baseEnergyLevel2, baseEnergyLevel3};
            return (long) (table[Math.clamp(unbreaking, 0, 3)] * PowerEntity.ONE_FE);
        }
    }

    private static long getBreakEnergy(float hardness, int efficiency, int unbreaking, int fortune, boolean silktouch, double breakBlockBase, double breakFortuneCoefficient, double breakEfficiencyCoefficient, double breakSilktouchCoefficient) {
        if (Float.isNaN(hardness) || hardness == 0) return 0;

        if (hardness < 0 || Float.isInfinite(hardness)) {
            return (long) (200 * breakBlockBase * (efficiency + 1) * PowerEntity.ONE_FE);
        }
        // Base energy, considering Fortune and Silktouch. Efficiency and Unbreaking should be calculated later.
        double base = breakBlockBase * Math.pow(breakFortuneCoefficient, fortune) * Math.pow(breakSilktouchCoefficient, silktouch ? 1 : 0);
        double coefficient = ((double) hardness) * Math.pow(breakEfficiencyCoefficient, efficiency) / (1 + Math.max(0, unbreaking));

        return (long) (coefficient * base * PowerEntity.ONE_FE);
    }

    public interface Default {
        Quarry QUARRY = new Quarry(
            10000.0,
            15.0,
            25.0,
            125.0,
            1.0,
            2.5,
            1.5848931924611136,
            1.379729661461215,
            1.5874010519681996,
            4.0
        );
        AdvQuarry ADV_QUARRY = new AdvQuarry(
            50000,
            15.0,
            30.0,
            125.0,
            1.25,
            2.5,
            1.379729661461215,
            1.5874010519681996,
            4.0
        );
        AdvPump ADV_PUMP = new AdvPump(
            1024,
            512,
            32,
            100,
            80,
            50,
            20
        );
    }
}
