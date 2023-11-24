package com.yogpc.qp.integration.rei;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.workbench.ScreenWorkbench;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@REIPluginClient
@OnlyIn(Dist.CLIENT)
public final class QuarryReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(WorkbenchRecipe.class, WorkbenchRecipe.RECIPE_TYPE, WorkbenchDisplay::new);
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerContainerClickArea(new Rectangle(7, 74, 161, 11), ScreenWorkbench.class, QuarryReiPlugin.WORKBENCH);
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new WorkbenchCategory());
        registry.addWorkstations(QuarryReiPlugin.WORKBENCH, EntryStacks.of(Holder.BLOCK_WORKBENCH));
    }
}
