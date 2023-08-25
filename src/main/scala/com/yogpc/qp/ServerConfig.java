package com.yogpc.qp;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ServerConfig {

    public final MachineWork machineWork;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        this.machineWork = new MachineWork(builder);
    }

    public static final class MachineWork {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> unworkableDimensions;

        private MachineWork(ForgeConfigSpec.Builder builder) {
            this.unworkableDimensions = builder
                .comment("Dimensions where machine should not work. This config is for admin of multi player server.")
                .worldRestart()
                .defineListAllowEmpty(List.of("unworkableDimensions"), List::of,
                    o -> o instanceof String s && ResourceLocation.isValidResourceLocation(s));
        }
    }
}
