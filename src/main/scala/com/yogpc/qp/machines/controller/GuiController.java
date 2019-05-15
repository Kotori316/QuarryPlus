package com.yogpc.qp.machines.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.controller.SetEntity;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiController extends GuiScreen {
    private GuiSlotEntityList slot;
    List<ResourceLocation> names;
    final int dim, xc, yc, zc;

    public GuiController(final int d, final int x, final int y, final int z, final List<ResourceLocation> l) {
        this.dim = d;
        this.xc = x;
        this.yc = y;
        this.zc = z;
        names = l.stream().sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
    }

    @Override
    public void initGui() {
        super.initGui();
        this.slot = new GuiSlotEntityList(this.mc, this.width, this.height, 30, this.height - 30, this);
        this.children.add(slot);
        addButton(new GuiButton(-1, this.width / 2 - 125, this.height - 26, 250, 20, I18n.format(TranslationKeys.DONE)) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                PacketHandler.sendToServer(SetEntity.create(dim, new BlockPos(xc, yc, zc), names.get(slot.selected())));
                mc.player.closeScreen();
            }
        });
        setFocused(slot);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        if (slot != null) {
            this.slot.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.render(mouseX, mouseY, partialTicks);
        drawCenteredString(this.fontRenderer, I18n.format(TranslationKeys.YOG_SPAWNER_SETTING), this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.mc.player.isAlive())
            this.mc.player.closeScreen();
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == this.mc.gameSettings.keyBindInventory.getKey().getKeyCode()) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }
}
