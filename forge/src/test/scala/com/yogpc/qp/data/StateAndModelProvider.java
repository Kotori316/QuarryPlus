package com.yogpc.qp.data;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.QPItem;
import com.yogpc.qp.machines.placer.PlacerBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

final class StateAndModelProvider extends BlockStateProvider {
    StateAndModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen.getPackOutput(), QuarryPlus.modID, exFileHelper);
    }

    private ResourceLocation blockTexture(String name) {
        return modLoc("block/" + name);
    }

    @Override
    public ResourceLocation blockTexture(Block block) {
        ResourceLocation name = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block), "Block %s isn't registered.".formatted(block));
        return new ResourceLocation(name.getNamespace(), "block" + "/" + name.getPath());
    }

    @Override
    protected void registerStatesAndModels() {
        // Blocks
        frame();
        dummyBlocks();
        placer();
        mining_well();
        markers();
        waterloggedMarkers();
        simpleBlockAndItemCubeAll(Holder.BLOCK_BOOK_MOVER);
        simpleBlockAndItemCubeAll(Holder.BLOCK_WORKBENCH);
        simpleBlockAndItemCubeAll(Holder.BLOCK_CONTROLLER);
        simpleBlockAndItemCubeAll(Holder.BLOCK_REMOTE_PLACER);
        workBlockAndItem(Holder.BLOCK_ADV_PUMP);
        workBlockAndItem(Holder.BLOCK_EXP_PUMP);
        simpleBlockAndItemCubeBottomTop(Holder.BLOCK_MOVER, blockTexture(Holder.BLOCK_MOVER), blockTexture("mover_top"), blockTexture("mover_bottom"));
        simpleBlockAndItemCubeBottomTop(Holder.BLOCK_PUMP, blockTexture("pump_side"), blockTexture("pump_top"), blockTexture("pump_bottom"));
        simpleBlockAndItemCubeBottomTop(Holder.BLOCK_REPLACER, blockTexture("replacer_side"), blockTexture("replacer_top"), blockTexture("replacer_bottom"));
        simpleBlockAndItemCubeBottomTop(Holder.BLOCK_FILLER, blockTexture("filler_side"), blockTexture("filler_top"), blockTexture("filler_top"));
        simpleBlockAndItemCubeBottomTop(Holder.BLOCK_CREATIVE_GENERATOR, blockTexture("replacer_bottom"), blockTexture("pump_bottom"), blockTexture("adv_pump_bottom"));
        workDirectionalBlockAndItem(Holder.BLOCK_QUARRY, "quarryplus");
        workDirectionalBlockAndItem(Holder.BLOCK_ADV_QUARRY);
        workDirectionalBlockAndItem(Holder.BLOCK_MINI_QUARRY);
        workDirectionalBlockAndItem(Holder.BLOCK_SOLID_FUEL_QUARRY);

        // Items
        simpleItem(Holder.ITEM_EXP_MODULE, "block/exp_pump_side");
        simpleItem(Holder.ITEM_FILLER_MODULE);
        simpleItem(Holder.ITEM_FILTER_MODULE, "item/void_module");
        simpleItem(Holder.ITEM_FUEL_MODULE_NORMAL);
        simpleItem(Holder.ITEM_PUMP_MODULE, "block/pump_side");
        simpleItem(Holder.ITEM_BEDROCK_MODULE, "item/bedrock_module");
        simpleItem(Holder.ITEM_REPLACER_MODULE, "block/replacer_side");
        simpleItem(Holder.ITEM_CHECKER);
        simpleItem(Holder.ITEM_Y_SETTER);
        simpleItem(Holder.ITEM_REPEAT_MODULE);
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
            .transform(ItemDisplayContext.GUI).translation(0, 0, 0).scale(0.8f).end()
            .transform(ItemDisplayContext.FIXED).translation(0, 0, 0).scale(0.8f).rotation(0, 90, 0).end()
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

    void workDirectionalBlockAndItem(QPBlock block) {
        workDirectionalBlockAndItem(block, block.getRegistryName().getPath());
    }

    void workDirectionalBlockAndItem(QPBlock block, String modelName) {
        var basePath = block.getRegistryName().getPath();
        var normalModel = models().orientableWithBottom("block/" + modelName,
            blockTexture(block),
            blockTexture(basePath + "_front"),
            blockTexture(block),
            blockTexture(basePath + "_top")
        );
        var workingModel = models().orientableWithBottom("block/" + modelName + "_working",
            blockTexture(block),
            blockTexture(basePath + "_front"),
            blockTexture(block),
            blockTexture(basePath + "_top_bb")
        );
        getVariantBuilder(block).forAllStates(blockState -> {
            var model = blockState.getValue(QPBlock.WORKING) ? workingModel : normalModel;
            var direction = blockState.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder()
                .modelFile(model)
                .rotationX(Math.floorMod(direction.getStepY() * -90, 360))
                .rotationY(direction.getAxis() == Direction.Axis.Y ? 0 : Math.floorMod(((int) direction.toYRot()) + 180, 360))
                .build();
        });
        simpleBlockItem(block, normalModel);
    }

    void simpleItem(QPItem item) {
        itemModels().basicItem(item.getRegistryName());
    }

    void simpleItem(QPItem item, String texture) {
        simpleItem(item, modLoc(texture));
    }

    void simpleItem(QPItem item, ResourceLocation texture) {
        itemModels().getBuilder("item/" + item.getRegistryName().getPath())
            .parent(new ModelFile.UncheckedModelFile("item/generated"))
            .texture("layer0", texture);
    }

    void placer() {
        PlacerBlock block = Holder.BLOCK_PLACER;
        var basePath = block.getRegistryName().getPath();
        var model = models().orientableWithBottom("block/" + basePath,
            blockTexture("plus_stone_side"),
            blockTexture("placer_front_horizontal"),
            blockTexture("plus_stone_top"),
            blockTexture("placer_front_vertical"));
        getVariantBuilder(block).forAllStatesExcept(blockState -> {
            var direction = blockState.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder()
                .modelFile(model)
                .rotationX(Math.floorMod(direction.getStepY() * -90, 360))
                .rotationY(direction.getAxis() == Direction.Axis.Y ? 0 : Math.floorMod(((int) direction.toYRot()) + 180, 360))
                .build();
        }, BlockStateProperties.TRIGGERED);
        simpleBlockItem(block, model);
    }

    void mining_well() {
        var block = Holder.BLOCK_MINING_WELL;
        var basePath = block.getRegistryName().getPath();
        var normalModel = models().cube("block/" + basePath,
            blockTexture(block), // Down
            blockTexture(basePath + "_top"), // Up
            blockTexture(basePath + "_front"), // North
            blockTexture(basePath + "_back"), // South
            blockTexture(block), // East
            blockTexture(block) // West
        ).texture("particle", blockTexture(block));
        var workingModel = models().cube("block/" + basePath + "_working",
            blockTexture(block), // Down
            blockTexture(basePath + "_top_w"), // Up
            blockTexture(basePath + "_front"), // North
            blockTexture(basePath + "_back"), // South
            blockTexture(block), // East
            blockTexture(block) // West
        ).texture("particle", blockTexture(block));
        getVariantBuilder(block).forAllStates(blockState -> {
            var model = blockState.getValue(QPBlock.WORKING) ? workingModel : normalModel;
            var direction = blockState.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder()
                .modelFile(model)
                .rotationX(Math.floorMod(direction.getStepY() * -90, 360))
                .rotationY(direction.getAxis() == Direction.Axis.Y ? 0 : Math.floorMod(((int) direction.toYRot()) + 180, 360))
                .build();
        });
        simpleBlockItem(block, normalModel);
    }

    void markers() {
        var model = models().getBuilder("block/marker_post")
            .texture("texture", blockTexture(Holder.BLOCK_MARKER))
            .texture("particle", blockTexture(Holder.BLOCK_MARKER))
            .element()
            // Pole
            .from(7.0f, 0.0f, 7.0f).to(9.0f, 10.0f, 9.0f)
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.UP) {
                    faceBuilder.texture("#texture").uvs(7.0f, 7.0f, 9.0f, 9.0f);
                } else if (direction == Direction.DOWN) {
                    faceBuilder.texture("#texture").uvs(0.0f, 0.0f, 2.0f, 2.0f).cullface(Direction.DOWN);
                } else {
                    faceBuilder.texture("#texture").uvs(7.0f, 6.0f, 9.0f, 16.0f);
                }
            })
            .end()
            // North
            .element()
            .from(7.0f, 7.0f, 6.0f).to(9.0f, 9.0f, 7.0f)
            .allFaces((direction, faceBuilder) -> {
                switch (direction.getAxis()) {
                    case X -> faceBuilder.texture("#texture").uvs(6.0f, 7.0f, 7.0f, 9.0f);
                    case Y -> faceBuilder.texture("#texture").uvs(7.0f, 6.0f, 9.0f, 7.0f);
                    case Z -> faceBuilder.texture("#texture").uvs(7.0f, 7.0f, 9.0f, 9.0f);
                }
                if (direction == Direction.SOUTH) faceBuilder.cullface(Direction.SOUTH);
            })
            .end()
            // South
            .element()
            .from(7.0f, 7.0f, 9.0f).to(9.0f, 9.0f, 10.0f)
            .allFaces((direction, faceBuilder) -> {
                switch (direction.getAxis()) {
                    case X -> faceBuilder.texture("#texture").uvs(6.0f, 7.0f, 7.0f, 9.0f);
                    case Y -> faceBuilder.texture("#texture").uvs(7.0f, 6.0f, 9.0f, 7.0f);
                    case Z -> faceBuilder.texture("#texture").uvs(7.0f, 7.0f, 9.0f, 9.0f);
                }
                if (direction == Direction.NORTH) faceBuilder.cullface(Direction.NORTH);
            })
            .end()
            // West
            .element()
            .from(6.0f, 7.0f, 7.0f).to(7.0f, 9.0f, 9.0f)
            .allFaces((direction, faceBuilder) -> {
                switch (direction.getAxis()) {
                    case X -> faceBuilder.texture("#texture").uvs(7.0f, 7.0f, 9.0f, 9.0f);
                    case Y, Z -> faceBuilder.texture("#texture").uvs(6.0f, 7.0f, 7.0f, 9.0f);
                }
                if (direction == Direction.EAST) faceBuilder.cullface(Direction.EAST);
            })
            .end()
            // East
            .element()
            .from(9.0f, 7.0f, 7.0f).to(10.0f, 9.0f, 9.0f)
            .allFaces((direction, faceBuilder) -> {
                switch (direction.getAxis()) {
                    case X -> faceBuilder.texture("#texture").uvs(7.0f, 7.0f, 9.0f, 9.0f);
                    case Y, Z -> faceBuilder.texture("#texture").uvs(6.0f, 7.0f, 7.0f, 9.0f);
                }
                if (direction == Direction.WEST) faceBuilder.cullface(Direction.WEST);
            })
            .end();

        directionalBlock(Holder.BLOCK_MARKER, model);
        itemModels().getBuilder(Holder.BLOCK_MARKER.getRegistryName().getPath())
            .parent(new ModelFile.UncheckedModelFile("item/generated"))
            .texture("layer0", new ResourceLocation(QuarryPlus.modID, "item/" + Holder.BLOCK_MARKER.getRegistryName().getPath() + "_item"));
        for (var marker : List.of(Holder.BLOCK_FLEX_MARKER, Holder.BLOCK_16_MARKER)) {
            var baseName = marker.getRegistryName().getPath();
            var m = models().withExistingParent("block/" + baseName, new ResourceLocation(QuarryPlus.modID, "block/marker_post"))
                .texture("texture", blockTexture(marker))
                .texture("particle", blockTexture(marker));
            simpleBlock(marker, m);
            itemModels().getBuilder(baseName)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", new ResourceLocation(QuarryPlus.modID, "item/" + baseName + "_item"));
        }
    }

    void waterloggedMarkers() {
        models().withExistingParent("block/waterlogged_marker_common", "block/block")
            // Center
            .element()
            .from(6, 4, 6).to(10, 12, 10)
            .allFaces((direction, faceBuilder) -> {
                switch (direction) {
                    case UP, DOWN -> faceBuilder.texture("#texture").uvs(7, 6, 8, 7);
                    default -> faceBuilder.texture("#texture").uvs(7, 7, 9, 9);
                }
            })
            .end()
            // North
            .element()
            .from(6, 6, 4).to(10, 10, 6)
            .rotation().angle(0).axis(Direction.Axis.Y).origin(8, 8, 8).end()
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.NORTH) {
                    faceBuilder.texture("#texture").uvs(7, 6, 8, 7);
                } else {
                    faceBuilder.texture("#texture").uvs(7, 7, 9, 9);
                }
            })
            .end()
            // South
            .element()
            .from(6, 6, 10).to(10, 10, 12)
            .rotation().angle(0).axis(Direction.Axis.Y).origin(8, 8, 8).end()
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.SOUTH) {
                    faceBuilder.texture("#texture").uvs(7, 6, 8, 7);
                } else {
                    faceBuilder.texture("#texture").uvs(7, 7, 9, 9);
                }
            })
            .end()
            // West
            .element()
            .from(4, 6, 6).to(6, 10, 10)
            .rotation().angle(0).axis(Direction.Axis.Y).origin(8, 8, 8).end()
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.WEST) {
                    faceBuilder.texture("#texture").uvs(7, 6, 8, 7);
                } else {
                    faceBuilder.texture("#texture").uvs(7, 7, 9, 9);
                }
            })
            .end()
            // East
            .element()
            .from(10, 6, 6).to(12, 10, 10)
            .rotation().angle(0).axis(Direction.Axis.Y).origin(8, 8, 8).end()
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.EAST) {
                    faceBuilder.texture("#texture").uvs(7, 6, 8, 7);
                } else {
                    faceBuilder.texture("#texture").uvs(7, 7, 9, 9);
                }
            })
            .end();
        for (var marker : List.of(Holder.BLOCK_WATERLOGGED_MARKER, Holder.BLOCK_WATERLOGGED_FLEX_MARKER, Holder.BLOCK_WATERLOGGED_16_MARKER)) {
            var baseName = marker.getRegistryName().getPath();
            var m = models().withExistingParent("block/" + baseName,
                    new ResourceLocation(QuarryPlus.modID, "block/waterlogged_marker_common"))
                .texture("texture", blockTexture(baseName.replace("waterlogged_", "")))
                .texture("particle", blockTexture(baseName.replace("waterlogged_", "")));
            simpleBlock(marker, m);
            simpleBlockItem(marker, m);
        }
    }
}
