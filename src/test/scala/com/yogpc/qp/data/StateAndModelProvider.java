package com.yogpc.qp.data;

import java.util.Objects;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

final class StateAndModelProvider extends BlockStateProvider {
    StateAndModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, QuarryPlus.modID, exFileHelper);
    }

    private ResourceLocation blockTexture(String name) {
        return modLoc("blocks/" + name);
    }

    @Override
    public ResourceLocation blockTexture(Block block) {
        ResourceLocation name = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block), "Block %s isn't registered.".formatted(block));
        return new ResourceLocation(name.getNamespace(), "blocks" + "/" + name.getPath());
    }

    @Override
    protected void registerStatesAndModels() {
        frame();
        dummyBlocks();
        simpleBlockAndItemCubeAll(Holder.BLOCK_BOOK_MOVER);
        simpleBlockAndItemCubeAll(Holder.BLOCK_WORKBENCH);
        simpleBlockAndItemCubeAll(Holder.BLOCK_CONTROLLER);
        workBlockAndItem(Holder.BLOCK_ADV_PUMP);
        workBlockAndItem(Holder.BLOCK_EXP_PUMP);
        simpleBlockAndItemCubeBottomTop(Holder.BLOCK_MOVER, blockTexture(Holder.BLOCK_MOVER), blockTexture("mover_top"), blockTexture("mover_bottom"));
        simpleBlockAndItemCubeBottomTop(Holder.BLOCK_PUMP, blockTexture("pump_side"), blockTexture("pump_top"), blockTexture("pump_bottom"));
        simpleBlockAndItemCubeBottomTop(Holder.BLOCK_CREATIVE_GENERATOR, blockTexture("replacer_bottom"), blockTexture("pump_bottom"), blockTexture("adv_pump_bottom"));
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

    void dummyBlocks() {
        var dummyReplacerModel = models().withExistingParent("block/dummy_replacer", new ResourceLocation("minecraft", "block/glass")).renderType("translucent");
        itemModels().withExistingParent("item/dummy_replacer", new ResourceLocation("minecraft", "block/glass"));
        var dummyBlockModel = models().cubeAll("block/dummy_block", blockTexture("dummy_block")).renderType("translucent");
        itemModels().withExistingParent("item/dummy", new ResourceLocation(QuarryPlus.modID, "block/dummy_block"));

        simpleBlock(Holder.BLOCK_DUMMY, dummyBlockModel);
        simpleBlock(Holder.BLOCK_DUMMY_REPLACER, dummyReplacerModel);
    }

    void simpleBlockAndItemCubeAll(Block block) {
        var model = cubeAll(block);
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    void simpleBlockAndItemCubeBottomTop(QPBlock block, ResourceLocation side, ResourceLocation top, ResourceLocation bottom) {
        var basePath = block.getRegistryName().getPath();
        var model = models().cubeBottomTop("block/" + basePath, side, bottom, top);
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    void workBlockAndItem(QPBlock block) {
        var basePath = block.getRegistryName().getPath();
        var normalModel = models().cubeBottomTop("block/" + basePath,
            blockTexture(basePath + "_side"),
            blockTexture(basePath + "_bottom"),
            blockTexture(basePath + "_top")
        );
        var workingModel = models().cubeBottomTop("block/" + basePath + "_working",
            blockTexture(basePath + "_side"),
            blockTexture(basePath + "_bottom"),
            blockTexture(basePath + "_top_w")
        );
        var builder = getVariantBuilder(block);
        builder.setModels(builder.partialState().with(QPBlock.WORKING, true), new ConfiguredModel(workingModel));
        builder.setModels(builder.partialState().with(QPBlock.WORKING, false), new ConfiguredModel(normalModel));
        simpleBlockItem(block, normalModel);
    }
}
