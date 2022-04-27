package com.yogpc.qp.gametest;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public final class MineableTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void checkMineable(GameTestHelper helper) {
        try (
            var stream = Utils.assertNotNull(getClass().getResourceAsStream("/data/minecraft/tags/blocks/mineable/pickaxe.json"));
            var reader = new InputStreamReader(stream)
        ) {
            var json = GsonHelper.parse(reader);
            var items = GsonHelper.getAsJsonArray(json, "values");
            for (JsonElement item : items) {
                var location = new ResourceLocation(item.getAsString());
                Utils.assertTrue(Registry.BLOCK.containsKey(location), "%s must exist in registry.".formatted(location));
            }
        } catch (IOException e) {
            throw new GameTestAssertException(e.getMessage());
        }
        helper.succeed();
    }
}
