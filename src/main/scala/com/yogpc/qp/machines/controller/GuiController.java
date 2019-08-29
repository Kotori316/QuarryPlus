package com.yogpc.qp.machines.controller;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.controller.SetEntity;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiController extends GuiScreen implements IHandleButton {
    private GuiSlotEntityList slot;
    private GuiTextField search;
    List<ResourceLocation> names;
    @javax.annotation.Nonnull
    private final List<ResourceLocation> allEntities;
    final int dim, xc, yc, zc;

    public GuiController(final int d, final int x, final int y, final int z, final List<ResourceLocation> allEntities) {
        this.dim = d;
        this.xc = x;
        this.yc = y;
        this.zc = z;
        this.allEntities = allEntities;
        names = allEntities.stream().sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
    }

    @Override
    public void initGui() {
        super.initGui();
        this.slot = new GuiSlotEntityList(this.mc, this.width, this.height, 30, this.height - 60, this);
        this.children.add(slot);
        addButton(new IHandleButton.Button(-1, this.width / 2 - 125, this.height - 26, 250, 20, I18n.format(TranslationKeys.DONE), this));
        setFocused(slot);
        this.search = new GuiTextField(0, fontRenderer, this.width / 2 - 125, this.height - 56, 250, 20);
        search.setText("");
        this.children.add(search);
        search.setCanLoseFocus(true);
        search.setTextAcceptHandler(this::searchEntities);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        if (slot != null) {
            this.slot.drawScreen(mouseX, mouseY, partialTicks);
            this.search.drawTextField(mouseX, mouseY, partialTicks);
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (!search.isFocused() && keyCode == this.mc.gameSettings.keyBindInventory.getKey().getKeyCode())) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == -1) {
            PacketHandler.sendToServer(SetEntity.create(dim, new BlockPos(xc, yc, zc), names.get(slot.selected())));
            mc.player.closeScreen();
        }
    }

    public void searchEntities(Integer id, String text) {
        List<ResourceLocation> collect;
        if (!text.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(text);
                collect = allEntities.stream().filter(l -> pattern.matcher(l.toString()).find())
                    .sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
            } catch (PatternSyntaxException e) {
                collect = allEntities.stream().filter(l -> l.toString().contains(text))
                    .sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
            }
        } else {
            collect = allEntities.stream().sorted(Comparator.comparing(ResourceLocation::getNamespace)).collect(Collectors.toList());
        }
        names = collect;
        this.slot.selected_$eq(0);
    }
}
