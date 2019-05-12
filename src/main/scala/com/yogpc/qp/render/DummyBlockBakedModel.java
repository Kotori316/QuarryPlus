package com.yogpc.qp.render;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

public class DummyBlockBakedModel implements IBakedModel {

    public DummyBlockBakedModel(IBakedModel model) {
        this.model = model;
    }

    public IBakedModel model;

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, Random rand) {
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
    public boolean isBuiltInRenderer() {
        return model.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return model.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return model.getOverrides();
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        return model.isAmbientOcclusion(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType cameraTransformType) {
        return model.handlePerspective(cameraTransformType);
    }
}
