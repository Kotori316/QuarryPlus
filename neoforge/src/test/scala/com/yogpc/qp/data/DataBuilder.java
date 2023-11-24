package com.yogpc.qp.data;

import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

interface DataBuilder {
    ResourceLocation location();

    JsonElement build(HolderLookup.Provider provider);
}
