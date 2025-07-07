package com.yogpc.qp.machine.placer;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public final class RemotePlacerScreen extends AbstractContainerScreen<PlacerContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/remote_replacer.png");

    public RemotePlacerScreen(PlacerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        {
            int oneBox = 18;
            int x = getMenu().startX - 1 + (getMenu().tile.getLastPlacedIndex() % 3) * oneBox;
            int y = 16 + (getMenu().tile.getLastPlacedIndex() / 3) * oneBox;
            int pX = leftPos + x;
            int pY = topPos + y;
            graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, pX, pY, 176, 0, oneBox, oneBox, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        var targetPos = getMenu().tile.getTargetPos();
        var color = targetPos.equals(getMenu().tile.getBlockPos()) ? 0xFF4040 : 0x404040;
        var x = 99;
        // 118, 22
        guiGraphics.drawString(font, "X: " + targetPos.getX(), x, 22, ARGB.opaque(color), false);
        guiGraphics.drawString(font, "Y: " + targetPos.getY(), x, 40, ARGB.opaque(color), false);
        guiGraphics.drawString(font, "Z: " + targetPos.getZ(), x, 58, ARGB.opaque(color), false);
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < Direction.Axis.VALUES.length; i++) {
            var yPos = topPos + 21 + i * 18;
            this.addRenderableWidget(
                Button.builder(Component.literal("-"), onPress(Direction.Axis.VALUES[i], Direction.AxisDirection.NEGATIVE))
                    .pos(leftPos + 80, yPos)
                    .size(18, 9)
                    .build()
            );
            this.addRenderableWidget(
                Button.builder(Component.literal("+"), onPress(Direction.Axis.VALUES[i], Direction.AxisDirection.POSITIVE))
                    .pos(leftPos + 151, yPos)
                    .size(18, 9)
                    .build()
            );
        }
    }

    private Button.OnPress onPress(Direction.Axis axis, Direction.AxisDirection axisDirection) {
        return button -> {
            if (getMenu().tile instanceof RemotePlacerEntity remotePlacer) {
                BlockPos newPos = remotePlacer.getTargetPos().relative(axis, axisDirection.getStep());
                remotePlacer.targetPos = newPos;
                PlatformAccess.getAccess().packetHandler().sendToServer(new RemotePlacerMessage(remotePlacer, newPos));
            }
        };
    }
}
