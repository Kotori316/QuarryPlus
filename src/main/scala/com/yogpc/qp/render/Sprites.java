package com.yogpc.qp.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// @Mod.EventBusSubscriber(modid = QuarryPlus.modID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class Sprites {
    public static final Sprites INSTANCE = new Sprites();
    private static final List<String> spriteNames = List.of("laser_1", "laser_2", "laser_3", "laser_4", "white", "stripes_h", "stripes_v", "stripes_blue", "stripes_red", "drill", "drill_head");
    private final Map<String, TextureAtlasSprite> spriteMap = new HashMap<>();

    private Sprites() {
    }

    @SubscribeEvent
    public static void register(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            spriteNames.stream().map(s -> new ResourceLocation(QuarryPlus.modID, "entities/" + s)).forEach(event::addSprite);
        }
    }

    @SubscribeEvent
    public static void registerSprites(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            spriteNames.forEach(s -> {
                var name = new ResourceLocation(QuarryPlus.modID, "entities/" + s);
                var sprite = event.getAtlas().getSprite(name);
                INSTANCE.spriteMap.put(s, sprite);
            });
        }
    }

    private TextureAtlasSprite getSprite(String name) {
        return spriteMap.computeIfAbsent(name, s ->
            Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(QuarryPlus.modID, "entities/" + s)));
    }

    public TextureAtlasSprite getMarkerBlue() {
        return getSprite("laser_4");
    }

    public TextureAtlasSprite getV() {
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
