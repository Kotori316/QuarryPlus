package com.yogpc.qp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class JsonReloadListener extends ReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int JSON_EXTENSION_LENGTH = ".json".length();
    private final Gson gson;
    private final String folder;

    public JsonReloadListener(Gson gson, String folder) {
        this.gson = gson;
        this.folder = folder;
    }

    /**
     * Performs any reloading that can be done off-thread, such as file IO
     */
    @Override
    protected Map<ResourceLocation, JsonElement> prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
        Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
        int i = this.folder.length() + 1;

        for (ResourceLocation dataPath : resourceManagerIn.getAllResourceLocations(this.folder, s -> !s.startsWith("_") && s.endsWith(".json"))) {
            String s = dataPath.getPath();
            ResourceLocation location = new ResourceLocation(dataPath.getNamespace(), s.substring(i, s.length() - JSON_EXTENSION_LENGTH));

            try (
                IResource iresource = resourceManagerIn.getResource(dataPath);
                InputStream inputstream = iresource.getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
            ) {
                JsonElement element = JSONUtils.fromJson(this.gson, reader, JsonElement.class);
                if (element != null) {
                    JsonElement put = map.put(location, element);
                    if (put != null) {
                        throw new IllegalStateException("Duplicate data file ignored with ID " + location);
                    }
                } else {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", location, dataPath);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}", location, dataPath, e);
            }
        }

        return map;
    }
}