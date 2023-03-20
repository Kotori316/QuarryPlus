package com.yogpc.qp.machines;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

/**
 * Just a util class to suppress parameter name warnings.
 */
public final class ScreenHelper {
    @SuppressWarnings("SuspiciousNameCombination") // The parameters in mappings are wrong.
    public static void blit(PoseStack pPoseStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        GuiComponent.blit(pPoseStack, pX, pY, pUOffset, pVOffset, pUWidth, pVHeight);
    }
}
