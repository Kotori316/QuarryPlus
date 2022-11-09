package com.yogpc.qp.integration.rei;

import java.util.ArrayList;
import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.PowerTile;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

final class WorkbenchCategory implements DisplayCategory<WorkbenchDisplay> {
    @Override
    public CategoryIdentifier<? extends WorkbenchDisplay> getCategoryIdentifier() {
        return QuarryReiPlugin.WORKBENCH;
    }

    @Override
    public List<Widget> setupDisplay(WorkbenchDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        int offset = 4;
        var inputs = display.getInputEntries();
        for (int i = 0; i < inputs.size(); i++) {
            EntryIngredient ingredient = inputs.get(i);
            var x = 18 * (i % 9) + bounds.x + offset;
            var y = 18 * (i / 9) + bounds.y + offset;
            var slot = Widgets.createSlot(new Point(x, y))
                .entries(ingredient)
                .markInput();
            widgets.add(slot);
        }

        widgets.add(Widgets.createResultSlotBackground(new Point(bounds.getCenterX() - 9, bounds.getMaxY() - 18 - offset * 2)));
        var output = Widgets.createSlot(new Point(bounds.getCenterX() - 9, bounds.getMaxY() - 18 - offset * 2))
            .entries(display.getOutputEntries().get(0))
            .markOutput()
            .disableBackground();
        widgets.add(output);
        widgets.add(Widgets.createLabel(new Point(bounds.x + offset, bounds.getMaxY() - 18 - offset * 2),
            Component.literal("%d FE".formatted(display.energy / PowerTile.ONE_FE))).leftAligned());
        return widgets;
    }

    @Override
    public Component getTitle() {
        return Holder.BLOCK_WORKBENCH.getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Holder.BLOCK_WORKBENCH);
    }

    @Override
    public int getDisplayHeight() {
        /*
        Should be 66 + 18, but there s no recipe that has 3 rows, so keep default value.
         */
        return 66;
    }

    @Override
    public int getDisplayWidth(WorkbenchDisplay display) {
        // To place 9 items in a row.
        return 150 + 18;
    }
}
