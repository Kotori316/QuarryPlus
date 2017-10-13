package com.yogpc.qp.gui;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.controller.SetEntity;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiController extends GuiScreen {
    private GuiSlotEntityList slot;
    final List<ResourceLocation> list;
    List<String> names;
    final int dim, xc, yc, zc;

    public GuiController(final int d, final int x, final int y, final int z, final List<ResourceLocation> l) {
        this.list = l;
        this.dim = d;
        this.xc = x;
        this.yc = y;
        this.zc = z;
        names = list.stream().sorted(Comparator.comparing(ResourceLocation::getResourceDomain)).map(ResourceLocation::toString).collect(Collectors.toList());
    }

    @Override
    public void initGui() {
        super.initGui();
        this.slot = new GuiSlotEntityList(this.mc, this.width, this.height, 30, this.height - 30, this);
        this.buttonList.add(new GuiButton(-1, this.width / 2 - 125, this.height - 26, 250, 20, I18n.format("gui.done")));
    }

    @Override
    public void actionPerformed(final GuiButton par1) {
        switch (par1.id) {
            case -1:
                PacketHandler.sendToServer(SetEntity.create(dim, new BlockPos(xc, yc, zc), list.get(slot.selected())));
                this.mc.player.closeScreen();
                break;
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        if (slot != null) {
            this.slot.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(this.fontRendererObj, I18n.format("yog.ctl.setting"), this.width / 2, 8, 0xFFFFFF);
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.slot.handleMouseInput();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!this.mc.player.isEntityAlive() || this.mc.player.isDead)
            this.mc.player.closeScreen();
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) {
        if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
    }
}
