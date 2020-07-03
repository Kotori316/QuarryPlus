package com.kotori316.marker.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.kotori316.marker.Marker;

@OnlyIn(Dist.CLIENT)
public class Resources {
    private Resources() {
    }

    private static final Resources instance = new Resources();

    public static Resources getInstance() {
        return instance;
    }

    public TextureAtlasSprite spriteWhite;

    @SubscribeEvent
    public void registerTexture(TextureStitchEvent.Pre event) {
        if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
            event.addSprite(new ResourceLocation(Marker.modID, "blocks/white"));
        }
    }

    @SubscribeEvent
    public void putTexture(TextureStitchEvent.Post event) {
        if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
            spriteWhite = event.getMap().getSprite(new ResourceLocation(Marker.modID, "blocks/white"));
        }
    }

}
