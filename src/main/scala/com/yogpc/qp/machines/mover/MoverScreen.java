package com.yogpc.qp.machines.mover;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MoverScreen extends AbstractContainerScreen<ContainerMover> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/mover.png");

    private final BlockPos pos;
    private IndexedButton enchantmentMoveButton;

    public MoverScreen(ContainerMover containerMarker, Inventory inventory, Component component) {
        super(containerMarker, inventory, component);
        this.pos = containerMarker.pos;
        // 176, 186
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void init() {
        super.init();
        var width = 120;
        this.addRenderableWidget(new IndexedButton(0, getGuiLeft() + (imageWidth - width) / 2, getGuiTop() + 20, width, 20, Component.translatable("FD.up"), this));
        enchantmentMoveButton = new IndexedButton(1, getGuiLeft() + (imageWidth - width) / 2, getGuiTop() + 40, width, 20, Component.literal(""), this);
        this.addRenderableWidget(enchantmentMoveButton);
        this.addRenderableWidget(new IndexedButton(2, getGuiLeft() + (imageWidth - width) / 2, getGuiTop() + 60, width, 20, Component.translatable("FD.down"), this));
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
        this.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        var enchantment = this.getMenu().getEnchantment();
        var name = enchantment.map(Enchantment::getDescriptionId).<Component>map(Component::translatable).orElse(Component.literal(""));
        enchantmentMoveButton.setMessage(name);
    }

    @Override
    public void onPress(Button button) {
        if (!button.active || getMenu().selected == null)
            return;
        if (button instanceof IndexedButton indexedButton)
            switch (indexedButton.id()) {
                case 1 -> moveEnchantment();
                case 0 -> selectPrevious();
                case 2 -> selectNext();
            }
    }

    private void selectNext() {
        var index = getMenu().movable.indexOf(getMenu().selected);
        if (index >= 0) {
            if (index == getMenu().movable.size() - 1) {
                // Last element. Move back to first.
                getMenu().selected = getMenu().movable.get(0);
            } else {
                getMenu().selected = getMenu().movable.get(index + 1);
            }
        }
    }

    private void selectPrevious() {
        var index = getMenu().movable.indexOf(getMenu().selected);
        if (index >= 0) {
            if (index == 0) {
                getMenu().selected = getMenu().movable.get(getMenu().movable.size() - 1);
            } else {
                getMenu().selected = getMenu().movable.get(index - 1);
            }
        }
    }

    private void moveEnchantment() {
        assert getMenu().selected != null;
        PacketHandler.sendToServer(new MoverMessage(pos, getMenu().containerId, getMenu().selected));
    }
}
