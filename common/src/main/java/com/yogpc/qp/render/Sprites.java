package com.yogpc.qp.render;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Sprites {
    public static final Sprites INSTANCE = new Sprites();
    private final Map<String, TextureAtlasSprite> spriteMap = new HashMap<>();

    private Sprites() {
    }

    @SuppressWarnings("deprecation")
    public static Identifier atlas() {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private TextureAtlasSprite getSprite(String name) {
        return spriteMap.computeIfAbsent(name, s -> {
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(atlas());
            if (texture instanceof TextureAtlas atlas) {
                return atlas.getSprite(getSpriteLocation(s));
            }
            return null;
        });
    }

    @NotNull
    private static Identifier getSpriteLocation(String s) {
        return Identifier.fromNamespaceAndPath(QuarryPlus.modID, "entity/" + s);
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

    public static RenderType cutout() {
        return RenderTypes.cutoutMovingBlock();
    }
}
