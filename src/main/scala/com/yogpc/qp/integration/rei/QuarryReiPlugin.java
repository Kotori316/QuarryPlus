package com.yogpc.qp.integration.rei;

import com.yogpc.qp.QuarryPlus;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.forge.REIPluginCommon;

@REIPluginCommon
public final class QuarryReiPlugin implements REIServerPlugin {
    static final CategoryIdentifier<WorkbenchDisplay> WORKBENCH = CategoryIdentifier.of(QuarryPlus.modID, "rei_workbenchplus");

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(WORKBENCH, WorkbenchDisplay.serializer());
    }
}
