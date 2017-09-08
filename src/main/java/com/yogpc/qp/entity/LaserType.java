package com.yogpc.qp.entity;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.util.ResourceLocation;

public enum LaserType {
    DRILL(255, 255, 0, 255) {
        @Override
        public ResourceLocation location() {
            return new ResourceLocation(QuarryPlus.modID, "blocks/blockdrilltexture");
        }
    },
    DRILL_HEAD(0, 0, 0, 255) {
        @Override
        public ResourceLocation location() {
            return new ResourceLocation(QuarryPlus.modID, "blocks/blockdrillheadtexture");
        }
    },
    BLUE_LASER(0, 0, 255, 255) {
        @Override
        public ResourceLocation location() {
            return new ResourceLocation(QuarryPlus.modID, "blocks/blockbluelaser");
        }
    },
    RED_LASER(255, 0, 0, 255) {
        @Override
        public ResourceLocation location() {
            return new ResourceLocation(QuarryPlus.modID, "blocks/blockredlaser");
        }
    };
    final int r, g, b, a;

    LaserType(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public float getr() {
        return r / 255;
    }

    public float getg() {
        return g / 255;
    }

    public float getb() {
        return b / 255;
    }

    public float geta() {
        return a / 255;
    }

    public abstract ResourceLocation location();
}
