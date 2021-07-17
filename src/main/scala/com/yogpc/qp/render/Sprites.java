package com.yogpc.qp.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class Sprites implements ClientSpriteRegistryCallback {
    public static final Sprites INSTANCE = new Sprites();
    private static final List<String> spriteNames = List.of("laser_1", "laser_2", "laser_3", "laser_4", "white", "stripes_h", "stripes_v", "stripes_b", "drill", "drill_head");
    private final Map<String, Sprite> spriteMap = new HashMap<>();

    private Sprites() {
    }

    public static void register() {
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(Sprites.INSTANCE);
    }

    @Override
    public void registerSprites(SpriteAtlasTexture atlasTexture, ClientSpriteRegistryCallback.Registry registry) {
        spriteNames.stream().map(s -> new Identifier(QuarryPlus.modID, "entities/" + s)).forEach(registry::register);
    }

    private Sprite getSprite(String name) {
        return spriteMap.computeIfAbsent(name, s ->
            MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier(QuarryPlus.modID, "entities/" + s)));
    }

    public Sprite getMarkerBlue() {
        return getSprite("laser_4");
    }

    public Sprite getFrameV() {
        return getSprite("stripes_v");
    }

    public Sprite getFrameH() {
        return getSprite("stripes_h");
    }

    public Sprite getBoxStripe() {
        return getSprite("stripes_b");
    }

    public Sprite getDrillStripe() {
        return getSprite("drill");
    }
    public Sprite getDrillHeadStripe() {
        return getSprite("drill_head");
    }
}
