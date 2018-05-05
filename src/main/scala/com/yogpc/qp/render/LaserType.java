package com.yogpc.qp.render;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.util.ResourceLocation;

public enum LaserType {
    DRILL(255, 255, 0, 255, "blocks/blockdrilltexture"),
    DRILL_HEAD(0, 0, 0, 255, "blocks/blockdrillheadtexture"),
    BLUE_LASER(0, 0, 255, 255, "blocks/blockbluelaser"),
    RED_LASER(255, 0, 0, 255, "blocks/blockredlaser");

    final int r, g, b, a;
    final ResourceLocation resourceLocation;
    public final scala.Symbol symbol = scala.Symbol.apply(name());

    LaserType(int r, int g, int b, int a, String l) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.resourceLocation = new ResourceLocation(QuarryPlus.modID, l);
    }

    public ResourceLocation location() {
        return resourceLocation;
    }
}
