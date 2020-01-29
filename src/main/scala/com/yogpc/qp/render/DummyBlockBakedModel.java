package com.yogpc.qp.render;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

public class DummyBlockBakedModel implements IBakedModel {

    public DummyBlockBakedModel(IBakedModel model) {
        this.model = model;
    }

    public IBakedModel model;

    @Override
    @SuppressWarnings("deprecation")
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return model.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return model.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return model.isGui3d();
    }

    @Override
    public boolean func_230044_c_() {
        return model.func_230044_c_();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return model.isBuiltInRenderer();
    }

    @Override
    @SuppressWarnings("deprecation") // Just Overriding
    public TextureAtlasSprite getParticleTexture() {
        return model.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return model.getOverrides();
    }

    @Override
    public boolean isAmbientOcclusion(BlockState state) {
        return model.isAmbientOcclusion(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public net.minecraft.client.renderer.model.ItemCameraTransforms getItemCameraTransforms() {
        return model.getItemCameraTransforms();
    }

}
