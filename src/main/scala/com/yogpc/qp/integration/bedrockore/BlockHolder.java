package com.yogpc.qp.integration.bedrockore;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

@SuppressWarnings("SpellCheckingInspection")
    /*package-private*/ class BlockHolder {

    public static final String MOD_ID_BEDROCK_ORES = "bedrockores";

    @GameRegistry.ObjectHolder(MOD_ID_BEDROCK_ORES)
    static class Obj {
        public static final Block bedrock_ore = null;
    }

    @SuppressWarnings("ConstantConditions")
    static boolean isBedrockOre(Block block) {
        // Refarence Obj.bedrock_ore is modified by forge.
        return Obj.bedrock_ore != null && block == Obj.bedrock_ore;
    }
}
