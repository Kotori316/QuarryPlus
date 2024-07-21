package com.yogpc.qp.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Unit is FE, not microFE, so I use double in this class
 */
public interface PowerMap {
    record Quarry(
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
        static final MapCodec<Quarry> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
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
            if (Float.isNaN(hardness) || hardness == 0) return 0;

            if (hardness < 0 || Float.isInfinite(hardness)) {
                return (long) (200 * breakBlockBase * (efficiency + 1) * PowerEntity.ONE_FE);
            }
            // Base energy, considering Fortune and Silktouch. Efficiency and Unbreaking should be calculated later.
            double base = breakBlockBase * Math.pow(breakFortuneCoefficient, fortune) * Math.pow(breakSilktouchCoefficient, silktouch ? 1 : 0);
            double coefficient = ((double) hardness) * Math.pow(breakEfficiencyCoefficient, efficiency) / (1 + Math.max(0, unbreaking));

            return (long) (coefficient * base * PowerEntity.ONE_FE);
        }
    }

    Quarry quarry();

    interface Default {
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
    }
}
