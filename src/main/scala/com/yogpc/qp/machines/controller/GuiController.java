package com.yogpc.qp.machines.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.controller.SetEntity;
import com.yogpc.qp.utils.Holder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiController extends Screen implements IHandleButton {
    private GuiSlotEntityList slot;
    List<ResourceLocation> names;
    final int dim, xc, yc, zc;

    public GuiController(final int d, final int x, final int y, final int z, final List<ResourceLocation> l) {
        super(new TranslationTextComponent(Holder.blockController().getTranslationKey()));
        this.dim = d;
        this.xc = x;
        this.yc = y;
        this.zc = z;
        names = l.stream().sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
    }

    @Override
    public void init() {
        super.init();
        this.slot = new GuiSlotEntityList(this.minecraft, this.width, this.height, 30, this.height - 30, this);
        this.children.add(slot);
        addButton(new IHandleButton.Button(-1, this.width / 2 - 125, this.height - 26, 250, 20, I18n.format(TranslationKeys.DONE), this));
        setFocused(slot);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        if (slot != null) {
            this.slot.render(mouseX, mouseY, partialTicks);
        }
        super.render(mouseX, mouseY, partialTicks);
        drawCenteredString(this.font, I18n.format(TranslationKeys.YOG_SPAWNER_SETTING), this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getMinecraft().player.isAlive())
            this.getMinecraft().player.closeScreen();
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == this.getMinecraft().gameSettings.keyBindInventory.getKey().getKeyCode()) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public void actionPerformed(IHandleButton.Button button) {
        if (button.id == -1) {
            PacketHandler.sendToServer(SetEntity.create(dim, new BlockPos(xc, yc, zc), names.get(slot.selected())));
            getMinecraft().player.closeScreen();
        }
    }
}
