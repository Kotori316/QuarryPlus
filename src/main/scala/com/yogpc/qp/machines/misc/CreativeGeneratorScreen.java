package com.yogpc.qp.machines.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class CreativeGeneratorScreen extends AbstractContainerScreen<CreativeGeneratorMenu> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/adv_pump.png");
    EditBox textFieldWidget;

    public CreativeGeneratorScreen(CreativeGeneratorMenu c, Inventory inventory, Component component) {
        super(c, inventory, component);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        com.yogpc.qp.machines.ScreenHelper.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        this.font.draw(matrices, getMenu().tile.sendEnergy / PowerTile.ONE_FE + " FE/t", this.titleLabelX, this.titleLabelY + 20, 4210752);
    }

    @Override
    protected void init() {
        super.init();
        textFieldWidget = new EditBox(this.font, getGuiLeft() + 8, getGuiTop() + 40, 130, 18, Component.literal("EnergyField"));
        textFieldWidget.setResponder(this::changeEnergy);
        textFieldWidget.setFilter(CreativeGeneratorScreen::canConvert);
        textFieldWidget.setCanLoseFocus(true);
        this.addRenderableWidget(textFieldWidget);
        textFieldWidget.setValue(String.valueOf(getMenu().tile.sendEnergy));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!textFieldWidget.isFocused() && this.getMinecraft().options.keyInventory.isActiveAndMatches(mouseKey))) {
            this.onClose();
            return true;
        }
        if (this.textFieldWidget.keyPressed(keyCode, scanCode, modifiers) || this.textFieldWidget.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void changeEnergy(String s) {
        try {
            long e = Long.parseLong(s);
            if (e >= 0) {
                getMenu().tile.sendEnergy = e;
                PacketHandler.sendToServer(new TileMessage(getMenu().tile));
            }
        } catch (NumberFormatException ignore) {
        }
    }

    private static boolean canConvert(String s) {
        try {
            return Long.parseLong(s) >= 0;
        } catch (NumberFormatException ignore) {
            return false;
        }
    }
}
