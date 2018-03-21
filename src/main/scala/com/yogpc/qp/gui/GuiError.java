package com.yogpc.qp.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;

public class GuiError extends GuiErrorScreen {

    private final GuiScreen parent;

    public GuiError(GuiScreen parent, String titleIn, String messageIn) {
        super(titleIn, messageIn);
        this.parent = parent;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        mc.displayGuiScreen(parent);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) this.mc.thePlayer.closeScreen();
    }
}
