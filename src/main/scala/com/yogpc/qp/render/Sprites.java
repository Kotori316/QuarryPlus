package com.yogpc.qp.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class Sprites implements ClientSpriteRegistryCallback {
    public static final Sprites INSTANCE = new Sprites();
    private static final List<String> spriteNames = List.of("laser_1", "laser_2", "laser_3", "laser_4", "white", "stripes_h", "stripes_v", "stripes_blue", "stripes_red", "drill", "drill_head");
    private final Map<String, TextureAtlasSprite> spriteMap = new HashMap<>();

    private Sprites() {
    }

    public static void register() {
        ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register(Sprites.INSTANCE);
    }

    @Override
    public void registerSprites(TextureAtlas atlasTexture, ClientSpriteRegistryCallback.Registry registry) {
        spriteNames.stream().map(s -> new ResourceLocation(QuarryPlus.modID, "entities/" + s)).forEach(registry::register);
    }

    private TextureAtlasSprite getSprite(String name) {
        return spriteMap.computeIfAbsent(name, s ->
            Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(QuarryPlus.modID, "entities/" + s)));
    }

    public TextureAtlasSprite getMarkerBlue() {
        return getSprite("laser_4");
    }

    public TextureAtlasSprite getFrameV() {
        return getSprite("stripes_v");
    }

    public TextureAtlasSprite getFrameH() {
        return getSprite("stripes_h");
    }

    public TextureAtlasSprite getBoxBlueStripe() {
        return getSprite("stripes_blue");
    }

    public TextureAtlasSprite getBoxRedStripe() {
        return getSprite("stripes_red");
    }

    public TextureAtlasSprite getDrillStripe() {
        return getSprite("drill");
    }

    public TextureAtlasSprite getDrillHeadStripe() {
        return getSprite("drill_head");
    }

    public TextureAtlasSprite getWhite() {
        return getSprite("white");
    }
}
