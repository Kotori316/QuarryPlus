/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.machines.item;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IHandleButton;
import com.yogpc.qp.machines.quarry.TileBasic;
import com.yogpc.qp.machines.workbench.BlockData;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.mover.EnchantmentMessage;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public class GuiEnchList extends ContainerScreen<ContainerEnchList> implements BooleanConsumer, IHandleButton {
    public static final int Toggle_id = 10, Remove_id = 12;
    private GuiSlotEnchList slot;
    private final TileBasic tile;
    private final Enchantment target;

    public GuiEnchList(ContainerEnchList c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);
        this.target = ForgeRegistries.ENCHANTMENTS.getValue(c.enchantmentName);
        this.tile = c.tile;
    }

    public boolean include() {
        if (this.target == Enchantments.FORTUNE)
            return this.tile.fortuneInclude;
        return this.tile.silktouchInclude;
    }

    private List<BlockData> getBlockDataList(Enchantment enchantment) {
        if (enchantment == Enchantments.SILK_TOUCH) {
            return tile.silktouchList;
        } else if (enchantment == Enchantments.FORTUNE) {
            return tile.fortuneList;
        } else {
            QuarryPlus.LOGGER.error(String.format("GuiEnchList target is %s", enchantment));
            return Collections.emptyList();
        }
    }

    @Override
    public void init() {
        this.xSize = this.width;
        this.ySize = this.height;
        super.init(); // must be here!
//        PacketHandler.sendToServer(BlockListRequestMessage.create(inventorySlots.windowId));
        addButton(new IHandleButton.Button(-1,
            this.width / 2 - 125, this.height - 26, 250, 20, I18n.format(TranslationKeys.DONE), this));
        addButton(new IHandleButton.Button(Toggle_id,
            this.width * 2 / 3 + 10, 140, 100, 20, "", this));
        addButton(new IHandleButton.Button(Remove_id,
            this.width * 2 / 3 + 10, 110, 100, 20, I18n.format(TranslationKeys.DELETE), this));
        this.slot = new GuiSlotEnchList(this.getMinecraft(), this.width * 3 / 5, this.height - 60, 30, this.height - 30,
            18, this);
        this.children.add(slot);
        this.setFocused(slot);
    }

    @Override
    public void actionPerformed(final IHandleButton.Button par1) {
        switch (par1.id) {
            case -1:
                this.getMinecraft().player.closeScreen();
                break;
            case Remove_id:
                this.getMinecraft().displayGuiScreen(new ConfirmScreen(this, new TranslationTextComponent(TranslationKeys.DELETE_BLOCK_SURE),
                    Optional.ofNullable(this.slot.getSelected()).map(GuiSlotEnchList.Entry::getData).map(BlockData::getDisplayText).orElse(new StringTextComponent("None"))));
                break;
            default: //maybe toggle
                PacketHandler.sendToServer(EnchantmentMessage.create(tile, EnchantmentMessage.Type.Toggle, target, BlockData.Invalid()));
                break;
        }
    }

    @Override
    public void accept(boolean result) {
        GuiSlotEnchList.Entry selected = this.slot.getSelected();
        if (selected != null && result) {
            final BlockData bd = selected.getData();
            PacketHandler.sendToServer(EnchantmentMessage.create(tile, EnchantmentMessage.Type.Remove, target, bd));

            getBlockDataList(target).remove(bd);
            refreshList();
        }
        this.getMinecraft().displayGuiScreen(this);
    }

    public void refreshList() {
        this.slot.refreshList();
        this.slot.setSelected(null);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float k, final int i, final int j) {
        if (slot != null)
            this.slot.render(i, j, k);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int i, final int j) {
        drawCenteredString(this.font, I18n.format(TranslationKeys.QP_ENABLE_LIST, I18n.format(this.target.getName())),
            this.xSize / 2, 8, 0xFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();
        this.buttons.get(1).setMessage(I18n.format(include() ? TranslationKeys.TOF_INCLUDE : TranslationKeys.TOF_EXCLUDE));
        this.buttons.get(2).active = !getBlockDataList(target).isEmpty();
    }

    public void buildModList(Consumer<GuiSlotEnchList.Entry> modListViewConsumer, Function<BlockData, GuiSlotEnchList.Entry> newEntry) {
        getBlockDataList(target).stream().map(newEntry).forEach(modListViewConsumer);
    }
}
