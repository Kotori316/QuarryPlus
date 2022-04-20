package com.yogpc.qp.machines.placer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class RemotePlacerScreen extends PlacerScreen {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/remote_replacer.png");

    public RemotePlacerScreen(PlacerContainer c, Inventory inventory, Component component) {
        super(c, inventory, component);
    }

    @Override
    protected ResourceLocation textureLocation() {
        return LOCATION;
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        var targetPos = getMenu().tile.getTargetPos();
        var x = 116;
        // 118, 22
        this.font.draw(matrices, "X: " + targetPos.getX(), x, 22, 0x404040);
        this.font.draw(matrices, "Y: " + targetPos.getY(), x, 40, 0x404040);
        this.font.draw(matrices, "Z: " + targetPos.getZ(), x, 58, 0x404040);
    }
}
