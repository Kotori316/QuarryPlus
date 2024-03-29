package com.yogpc.qp.machines.placer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public final class RemotePlacerScreen extends PlacerScreen implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/remote_replacer.png");

    public RemotePlacerScreen(PlacerContainer c, Inventory inventory, Component component) {
        super(c, inventory, component);
    }

    @Override
    protected ResourceLocation textureLocation() {
        return LOCATION;
    }

    @Override
    protected void init() {
        super.init();
        var counter = new AtomicInteger(0);
        for (int i = 0; i < 3; i++) {
            var yPos = getGuiTop() + 21 + i * 18;
            this.addRenderableWidget(new IndexedButton(counter.getAndIncrement(), getGuiLeft() + 97, yPos, 18, 9, Component.literal("-"), this));
            this.addRenderableWidget(new IndexedButton(counter.getAndIncrement(), getGuiLeft() + 151, yPos, 18, 9, Component.literal("+"), this));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        var targetPos = getMenu().tile.getTargetPos();
        var color = targetPos.equals(getMenu().tile.getBlockPos()) ? 0xFF4040 : 0x404040;
        var x = 116;
        // 118, 22
        graphics.drawString(this.font, "X: " + targetPos.getX(), x, 22, color, false);
        graphics.drawString(this.font, "Y: " + targetPos.getY(), x, 40, color, false);
        graphics.drawString(this.font, "Z: " + targetPos.getZ(), x, 58, color, false);
    }

    @Override
    protected void renderModeLabel(GuiGraphics graphics) {
        // Mode
        PlacerTile.RedstoneMode mode = this.getMenu().tile.redstoneMode;
        String pA = mode.isAlways() ? "Always" : "Pulse";
        String rs;
        if (mode.isRsOn()) rs = "RS On";
        else if (mode.isRsOff()) rs = "RS Off";
        else rs = "";
        String only;
        if (mode.canBreak() && !mode.canPlace()) only = "Break";
        else if (mode.canPlace() && !mode.canBreak()) only = "Place";
        else only = "";

        graphics.drawString(this.font, pA, 90, 6, 0x404040, false);
        graphics.drawString(this.font, only, 130, 6, 0x404040, false);
        graphics.drawString(this.font, rs, 100, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void onPress(Button pButton) {
        if (pButton instanceof IndexedButton indexedButton && getMenu().tile instanceof RemotePlacerTile remotePlacerTile) {
            int amount = switch (indexedButton.getIndex() % 2) {
                case 0 -> -1;
                case 1 -> +1;
                default -> throw new AssertionError("X % 2 must in 0-1.");
            } * (Screen.hasShiftDown() ? 16 : Screen.hasControlDown() ? 4 : 1);
            Direction.Axis axis = switch (indexedButton.getIndex() / 2) {
                case 0 -> Direction.Axis.X;
                case 1 -> Direction.Axis.Y;
                case 2 -> Direction.Axis.Z;
                default -> throw new IllegalArgumentException("Bad index " + indexedButton.getIndex());
            };
            BlockPos newPos = remotePlacerTile.getTargetPos().relative(axis, amount);
            remotePlacerTile.targetPos = newPos;
            PacketHandler.sendToServer(new RemotePlacerMessage(remotePlacerTile, newPos));
        }
    }

    private int getGuiTop() {
        return topPos;
    }

    private int getGuiLeft() {
        return leftPos;
    }

}
