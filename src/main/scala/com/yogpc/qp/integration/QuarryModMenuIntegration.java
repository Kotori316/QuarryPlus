package com.yogpc.qp.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.yogpc.qp.QuarryConfig;
import me.shedaniel.autoconfig.AutoConfig;

public class QuarryModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(QuarryConfig.class, parent).get();
    }
}
