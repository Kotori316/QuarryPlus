package com.yogpc.qp.integration.crafttweaker;

import java.util.ArrayList;
import java.util.List;

import com.yogpc.qp.QuarryPlus;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import javax.annotation.Nullable;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = QuarryPlus.modID + "_ct", name = QuarryPlus.Mod_Name + "_CT", version = "${version}", certificateFingerprint = "@FINGERPRINT@")
public class CT {

    public static final String CRAFT_TWEAKER_ID = "crafttweaker";
    @Nullable
    private Object module;
    static final List<Object> actions = new ArrayList<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata metadata = event.getModMetadata();
        metadata.parent = QuarryPlus.modID;
        if (Loader.isModLoaded(CRAFT_TWEAKER_ID)) {
            try {
                module = new Module();
                ((Module) module).preInit();
            } catch (RuntimeException e) {
                QuarryPlus.LOGGER.error("Exception occurred in loading CraftTweaker.", e);
                module = null;
            }
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (module != null) {
            try {
                ((Module) module).postInit();
            } catch (RuntimeException e) {
                QuarryPlus.LOGGER.error("Exception occurred in loading CraftTweaker.", e);
                module = null;
            }
        }
    }

    private static class Module {
        private void preInit() {
            CraftTweakerAPI.registerClass(WorkBenchCTRegister.class);
        }

        private void postInit() {
            actions.stream().map(o -> (IAction) o).forEach(CraftTweakerAPI::apply);
        }
    }
}
