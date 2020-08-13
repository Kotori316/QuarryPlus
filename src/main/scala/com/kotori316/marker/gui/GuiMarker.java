package com.kotori316.marker.gui;

import java.util.stream.Stream;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.machines.base.ScreenUtil;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import com.kotori316.marker.Marker;
import com.kotori316.marker.TileFlexMarker;
import com.kotori316.marker.packet.ButtonMessage;
import com.kotori316.marker.packet.PacketHandler;

public class GuiMarker extends ContainerScreen<ContainerMarker> {
    private static final ResourceLocation LOCATION = new ResourceLocation(Marker.modID, "textures/gui/marker.png");
    private static final String[] upSide = {"UP"};
    private static final String[] center = {"Left", "Forward", "Right"};
    private static final String[] downSide = {"Down"};
    private static final int[] amounts = {-16, -1, 1, 16};

    public GuiMarker(ContainerMarker containerMarker, PlayerInventory inv, ITextComponent component) {
        super(containerMarker, inv, component);
        //217, 188
        this.xSize = 217;
        this.ySize = 188;
        this.playerInventoryTitleY = this.ySize - 96 + 2; // y position of text, inventory
    }

    @Override
    public void init() {
        super.init();
        StringTextComponent[] mp = Stream.of("--", "-", "+", "++").map(StringTextComponent::new).toArray(StringTextComponent[]::new);
        int w = 10;
        int h = 20;
        int top = 16;

        for (int i = 0; i < upSide.length; i++) {
            for (int j = 0; j < mp.length; j++) {
                addButton(new Button(guiLeft + xSize / 2 - 4 * w * upSide.length / 2 + i * w * mp.length + w * j, guiTop + top, w, h, mp[j], this::actionPerformed));
            }
        }
        for (int i = 0; i < center.length; i++) {
            for (int j = 0; j < mp.length; j++) {
                addButton(new Button(guiLeft + xSize / 2 - 4 * w * center.length / 2 + i * w * mp.length + w * j, guiTop + top + 35, w, h, mp[j], this::actionPerformed));
            }
        }
        for (int i = 0; i < downSide.length; i++) {
            for (int j = 0; j < mp.length; j++) {
                addButton(new Button(guiLeft + xSize / 2 - 4 * w * downSide.length / 2 + i * w * mp.length + w * j, guiTop + top + 70, w, h, mp[j], this::actionPerformed));
            }
        }

    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack);// back ground
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        ScreenUtil.color4f();
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        String s = I18n.format(TileFlexMarker.Movable.UP.transName);
        font.drawString(matrixStack, s, this.xSize / 2 - font.getStringWidth(s) / 2, 6, 0x404040);
        s = I18n.format(TileFlexMarker.Movable.FORWARD.transName);
        font.drawString(matrixStack, s, this.xSize / 2 - font.getStringWidth(s) / 2, 6 + 35, 0x404040);
        s = I18n.format(TileFlexMarker.Movable.LEFT.transName);
        font.drawString(matrixStack, s, this.xSize / 2 - font.getStringWidth(s) / 2 - 40, 6 + 35, 0x404040);
        s = I18n.format(TileFlexMarker.Movable.RIGHT.transName);
        font.drawString(matrixStack, s, this.xSize / 2 - font.getStringWidth(s) / 2 + 40, 6 + 35, 0x404040);
        s = I18n.format(TileFlexMarker.Movable.DOWN.transName);
        font.drawString(matrixStack, s, this.xSize / 2 - font.getStringWidth(s) / 2, 6 + 70, 0x404040);
    }

    public void actionPerformed(Button button) {
        int id = this.buttons.indexOf(button);
        if (id >= 0) {
            TileFlexMarker.Movable movable = TileFlexMarker.Movable.valueOf(id / 4);
            ButtonMessage message = new ButtonMessage(container.pos, PacketHandler.getDimId(container.player.world), movable, amounts[id % 4]);
            PacketHandler.sendToServer(message);
        }
    }
}
