package com.yogpc.qp.machines.generator.creative;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.ScreenUtil;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class CreativeGeneratorGui extends ContainerScreen<CreativeGeneratorContainer> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png");
    private TextFieldWidget textFieldWidget;

    public CreativeGeneratorGui(CreativeGeneratorContainer c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);
    }

    @Override
    protected void init() {
        super.init();
        textFieldWidget = new TextFieldWidget(this.font, getGuiLeft() + 8, getGuiTop() + 30, 100, 18, new StringTextComponent("EnergyField"));
        textFieldWidget.setResponder(this::changeEnergy);
        textFieldWidget.setValidator(CreativeGeneratorGui::canConvert);
        textFieldWidget.setCanLoseFocus(true);
        this.children.add(textFieldWidget);
        textFieldWidget.setText(String.valueOf(container.generatorTile.sendAmount));
    }

    private void changeEnergy(String s) {
        try {
            long e = Long.parseLong(s);
            if (e >= 0) {
                container.generatorTile.sendAmount = e;
                PacketHandler.sendToServer(TileMessage.create(container.generatorTile));
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!textFieldWidget.isFocused() && this.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))) {
            this.closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack);// back ground
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
        textFieldWidget.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int x, int y) {
        ScreenUtil.color4f();
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.blit(matrix, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        String s = container.generatorTile.energyInFE() + " FE/t";
        font.drawString(matrixStack, s, getXSize() - font.getStringWidth(s) - 20, 60, 0x404040);
    }
}
