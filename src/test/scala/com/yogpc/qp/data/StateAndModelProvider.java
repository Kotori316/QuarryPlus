package com.yogpc.qp.data;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

final class StateAndModelProvider extends BlockStateProvider {
    StateAndModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, QuarryPlus.modID, exFileHelper);
    }

    private ResourceLocation blockTexture(String name) {
        return modLoc("blocks/" + name);
    }

    @Override
    protected void registerStatesAndModels() {
        frame();
    }

    void frame() {
        var center = models().getBuilder("block/frame_post")
            .renderType("cutout")
            .texture("texture", blockTexture("frame"))
            .texture("particle", blockTexture("frame"))
            .element().from(4.0f, 4.0f, 4.0f).to(12.0f, 12.0f, 12.0f)
            .allFaces((direction, faceBuilder) -> faceBuilder.uvs(4, 4, 12, 12).texture("#texture"))
            .end();
        var side = models().getBuilder("block/frame_side")
            .renderType("cutout")
            .texture("texture", blockTexture("frame"))
            .texture("particle", blockTexture("frame"))
            .element().from(4, 4, 0).to(12, 12, 4)
            .face(Direction.DOWN).uvs(4, 0, 12, 4).texture("#texture").end()
            .face(Direction.UP).uvs(4, 0, 12, 4).texture("#texture").end()
            .face(Direction.SOUTH).uvs(4, 4, 12, 12).texture("#texture").cullface(Direction.SOUTH).end()
            .face(Direction.WEST).uvs(0, 4, 4, 12).texture("#texture").end()
            .face(Direction.EAST).uvs(0, 4, 4, 12).texture("#texture").end()
            .end();

        getMultipartBuilder(Holder.BLOCK_FRAME).part()
            .modelFile(center).addModel().end().part()
            .modelFile(side).uvLock(true).addModel().condition(BlockStateProperties.NORTH, true).end().part()
            .modelFile(side).uvLock(true).rotationY(90).addModel().condition(BlockStateProperties.EAST, true).end().part()
            .modelFile(side).uvLock(true).rotationY(180).addModel().condition(BlockStateProperties.SOUTH, true).end().part()
            .modelFile(side).uvLock(true).rotationY(270).addModel().condition(BlockStateProperties.WEST, true).end().part()
            .modelFile(side).uvLock(true).rotationX(270).addModel().condition(BlockStateProperties.UP, true).end().part()
            .modelFile(side).uvLock(true).rotationX(90).addModel().condition(BlockStateProperties.DOWN, true).end()
        ;
        itemModels().withExistingParent("item/frame", "block/block")
            .transforms()
            .transform(ItemTransforms.TransformType.GUI).translation(0, 0, 0).scale(0.8f).end()
            .transform(ItemTransforms.TransformType.FIXED).translation(0, 0, 0).scale(0.8f).rotation(0, 90, 0).end()
            .end()
            .texture("texture", blockTexture("frame"))
            .element().from(4, 0, 4).to(12, 12, 12)
            .allFaces((direction, faceBuilder) -> faceBuilder.uvs(4.0f, 4.0f, 12.0f, direction.getAxis() == Direction.Axis.Y ? 12.0f : 16.0f).texture("#texture"))
        ;
    }
}
