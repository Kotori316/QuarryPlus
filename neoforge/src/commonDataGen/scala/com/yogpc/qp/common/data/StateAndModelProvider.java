package com.yogpc.qp.common.data;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpBlockProperty;
import com.yogpc.qp.machine.QpItem;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.List;

final class StateAndModelProvider extends ModelProvider {
    StateAndModelProvider(PackOutput output) {
        super(output, QuarryPlus.modID);
    }

    private ResourceLocation blockTexture(String name) {
        return modLocation("block/" + name);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var holder = PlatformAccess.getAccess().registerObjects();
        // Blocks
        frame(blockModels, itemModels);
        dummyBlocks(blockModels, itemModels);
        placer(blockModels);
        mining_well(blockModels, itemModels);
        markers(blockModels, itemModels);
        waterloggedMarkers(blockModels, itemModels);
        // simpleBlockAndItemCubeAll(blockModels, itemModels,Holder.BLOCK_BOOK_MOVER);
        // simpleBlockAndItemCubeAll(blockModels, itemModels,Holder.BLOCK_WORKBENCH);
        // simpleBlockAndItemCubeAll(blockModels, itemModels,Holder.BLOCK_CONTROLLER);
        simpleBlockAndItemCubeAll(blockModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.BLOCK_REMOTE_PLACER.get());
        simpleBlockAndItemCubeAll(blockModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.BLOCK_DEBUG_STORAGE.get());
        // workBlockAndItem(blockModels, itemModels, Holder.BLOCK_ADV_PUMP);
        // workBlockAndItem(blockModels, itemModels, Holder.BLOCK_EXP_PUMP);
        simpleBlockAndItemCubeBottomTop(blockModels, holder.moverBlock().get(), blockTexture(holder.moverBlock().get().name.getPath()), blockTexture("mover_top"), blockTexture("mover_bottom"));
        // simpleBlockAndItemCubeBottomTop(blockModels, itemModels, Holder.BLOCK_PUMP, blockTexture("pump_side"), blockTexture("pump_top"), blockTexture("pump_bottom"));
        // simpleBlockAndItemCubeBottomTop(blockModels, itemModels, Holder.BLOCK_REPLACER, blockTexture("replacer_side"), blockTexture("replacer_top"), blockTexture("replacer_bottom"));
        // simpleBlockAndItemCubeBottomTop(blockModels, itemModels, Holder.BLOCK_FILLER, blockTexture("filler_side"), blockTexture("filler_top"), blockTexture("filler_top"));
        simpleBlockAndItemCubeBottomTop(blockModels, holder.generatorBlock().get(), blockTexture("replacer_bottom"), blockTexture("pump_bottom"), blockTexture("adv_pump_bottom"));
        workDirectionalBlockAndItem(blockModels, holder.quarryBlock().get());
        workDirectionalBlockAndItem(blockModels, holder.advQuarryBlock().get());
        // workDirectionalBlockAndItem(blockModels, itemModels,Holder.BLOCK_MINI_QUARRY);
        // workDirectionalBlockAndItem(blockModels, itemModels,Holder.BLOCK_SOLID_FUEL_QUARRY);

        // Items
        simpleItem(itemModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.ITEM_EXP_MODULE.get(), "block/exp_pump_side");
        // simpleItem(blockModels, itemModels,Holder.ITEM_FILLER_MODULE);
        simpleItem(itemModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.ITEM_FILTER_MODULE.get(), "item/void_module");
        // simpleItem(blockModels, itemModels,Holder.ITEM_FUEL_MODULE_NORMAL);
        simpleItem(itemModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.ITEM_PUMP_MODULE.get(), "block/pump_side");
        simpleItem(itemModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.ITEM_BEDROCK_MODULE.get(), "item/bedrock_module");
        // simpleItem(blockModels, itemModels,Holder.ITEM_REPLACER_MODULE, "block/replacer_side");
        simpleItem(itemModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.ITEM_CHECKER.get());
        simpleItem(itemModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.ITEM_Y_SET.get());
        simpleItem(itemModels, PlatformAccessNeoForge.RegisterObjectsNeoForge.ITEM_REPEAT_MODULE.get());
    }

    void frame(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var centerTemplate = ExtendedModelTemplateBuilder.builder()
            .renderType(renderTypeName(RenderType.cutout()))
            .element(b ->
                b.from(4.0f, 4.0f, 4.0f).to(12.0f, 12.0f, 12.0f)
                    .allFaces((direction, faceBuilder) -> faceBuilder.uvs(4, 4, 12, 12).texture(TextureSlot.TEXTURE))
            )
            .requiredTextureSlot(TextureSlot.TEXTURE)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build();
        var center = centerTemplate.create(modLocation("block/frame_post"),
            new TextureMapping()
                .put(TextureSlot.TEXTURE, blockTexture("frame"))
                .put(TextureSlot.PARTICLE, blockTexture("frame")),
            blockModels.modelOutput
        );
        var sideTemplate = ExtendedModelTemplateBuilder.builder()
            .renderType(renderTypeName(RenderType.cutout()))
            .element(b -> b.from(4, 4, 0).to(12, 12, 4)
                .face(Direction.DOWN, f -> f.uvs(4, 0, 12, 4).texture(TextureSlot.TEXTURE))
                .face(Direction.UP, f -> f.uvs(4, 0, 12, 4).texture(TextureSlot.TEXTURE))
                .face(Direction.SOUTH, f -> f.uvs(4, 4, 12, 12).texture(TextureSlot.TEXTURE).cullface(Direction.SOUTH))
                .face(Direction.WEST, f -> f.uvs(0, 4, 4, 12).texture(TextureSlot.TEXTURE))
                .face(Direction.EAST, f -> f.uvs(0, 4, 4, 12).texture(TextureSlot.TEXTURE))
            )
            .requiredTextureSlot(TextureSlot.TEXTURE)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build();
        var side = sideTemplate.create(modLocation("block/frame_side"),
            new TextureMapping()
                .put(TextureSlot.TEXTURE, blockTexture("frame"))
                .put(TextureSlot.PARTICLE, blockTexture("frame")),
            blockModels.modelOutput);

        var block = PlatformAccess.getAccess().registerObjects().frameBlock().get();
        blockModels.blockStateOutput.accept(
            MultiPartGenerator.multiPart(block)
                .with(Variant.variant().with(VariantProperties.MODEL, center))
                .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, side).with(VariantProperties.UV_LOCK, true))
                .with(Condition.condition().term(BlockStateProperties.EAST, true), Variant.variant().with(VariantProperties.MODEL, side).with(VariantProperties.UV_LOCK, true).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, side).with(VariantProperties.UV_LOCK, true).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .with(Condition.condition().term(BlockStateProperties.WEST, true), Variant.variant().with(VariantProperties.MODEL, side).with(VariantProperties.UV_LOCK, true).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .with(Condition.condition().term(BlockStateProperties.UP, true), Variant.variant().with(VariantProperties.MODEL, side).with(VariantProperties.UV_LOCK, true).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                .with(Condition.condition().term(BlockStateProperties.DOWN, true), Variant.variant().with(VariantProperties.MODEL, side).with(VariantProperties.UV_LOCK, true).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
        );

        var itemTemplate = ExtendedModelTemplateBuilder.builder()
            .parent(mcLocation("block/block"))
            .transform(ItemDisplayContext.GUI, b -> b.translation(0, 0, 0).scale(0.8f))
            .transform(ItemDisplayContext.FIXED, b -> b.translation(0, 0, 0).scale(0.8f).rotation(0, 90, 0))
            .requiredTextureSlot(TextureSlot.TEXTURE)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(b -> b.from(4, 0, 4).to(12, 12, 12)
                .allFaces((direction, faceBuilder) -> faceBuilder.uvs(4.0f, 4.0f, 12.0f, direction.getAxis() == Direction.Axis.Y ? 12.0f : 16.0f).texture(TextureSlot.TEXTURE)))
            .build();
        var item = itemTemplate.create(modLocation("item/frame"),
            new TextureMapping()
                .put(TextureSlot.TEXTURE, blockTexture("frame"))
                .put(TextureSlot.PARTICLE, blockTexture("frame")),
            itemModels.modelOutput);
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(item));
    }

    void dummyBlocks(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
       /* var dummyReplacerTemplate = ExtendedModelTemplateBuilder.builder()
            .parent(mcLocation("block/glass"))
            .renderType(renderTypeName(RenderType.translucent()))
            .build();
        var dummyReplacerModel = dummyReplacerTemplate.create(modLocation("block/dummy_replacer"), new TextureMapping(), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(PlatformAccessNeoForge.RegisterObjectsNeoForge.BLOCK_.get(), dummyReplacerModel));
        blockModels.registerSimpleItemModel(PlatformAccessNeoForge.RegisterObjectsNeoForge.BLOCK_.get(), dummyReplacerModel);*/

        var dummyBlockTemplate = ModelTemplates.CUBE_ALL.extend().renderType(renderTypeName(RenderType.translucent())).build();
        var dummyBlockModel = dummyBlockTemplate.create(modLocation("block/dummy_block"), TextureMapping.cube(blockTexture("dummy_block")), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(PlatformAccessNeoForge.RegisterObjectsNeoForge.BLOCK_SOFT.get(), dummyBlockModel));
        blockModels.registerSimpleItemModel(PlatformAccessNeoForge.RegisterObjectsNeoForge.BLOCK_SOFT.get(), dummyBlockModel);
    }

    void simpleBlockAndItemCubeAll(BlockModelGenerators blockModels, QpBlock block) {
        blockModels.createTrivialCube(block);
    }

    void simpleBlockAndItemCubeBottomTop(BlockModelGenerators blockModels, QpBlock block, ResourceLocation side, ResourceLocation top, ResourceLocation bottom) {
        var model = ModelTemplates.CUBE_BOTTOM_TOP.create(block,
            new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top),
            blockModels.modelOutput
        );
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, model));
        blockModels.registerSimpleItemModel(block, model);
    }

    void workBlockAndItem(BlockModelGenerators blockModels, QpBlock block) {
        var basePath = block.name.getPath();
        var normalModel = TexturedModel.CUBE_TOP_BOTTOM.create(block, blockModels.modelOutput);
        var workingModel = TexturedModel.CUBE_TOP_BOTTOM.updateTexture(m -> m.put(TextureSlot.TOP, blockTexture(basePath + "_top_w"))).create(block, blockModels.modelOutput);

        var workingProperty = PropertyDispatch.properties(BlockStateProperties.FACING, QpBlockProperty.WORKING)
            .generate((direction, working) -> {
                if (working) {
                    return Variant.variant().with(VariantProperties.MODEL, workingModel);
                } else {
                    return Variant.variant().with(VariantProperties.MODEL, normalModel);
                }
            });
        var builder = MultiVariantGenerator.multiVariant(block).with(workingProperty);
        blockModels.blockStateOutput.accept(builder);
        blockModels.registerSimpleItemModel(block, normalModel);
    }

    static Variant rotate(Direction direction, Variant v) {
        var rotX = rotationFromValue(Math.floorMod(direction.getStepY() * -90, 360));
        var rotY = rotationFromValue(direction.getAxis() == Direction.Axis.Y ? 0 : Math.floorMod(((int) direction.toYRot()) + 180, 360));
        if (rotX != VariantProperties.Rotation.R0) {
            v.with(VariantProperties.X_ROT, rotX);
        }
        if (rotY != VariantProperties.Rotation.R0) {
            v.with(VariantProperties.Y_ROT, rotY);
        }
        return v;
    }

    void workDirectionalBlockAndItem(BlockModelGenerators blockModels, QpBlock block) {
        var basePath = block.name.getPath();
        var blockTexture = TextureMapping.getBlockTexture(block);
        var normalModel = TexturedModel.ORIENTABLE.updateTexture(m -> m.put(TextureSlot.SIDE, blockTexture).put(TextureSlot.BOTTOM, blockTexture)).create(block, blockModels.modelOutput);
        var workingModel = TexturedModel.ORIENTABLE.updateTexture(m -> m.put(TextureSlot.SIDE, blockTexture).put(TextureSlot.BOTTOM, blockTexture).put(TextureSlot.TOP, blockTexture(basePath + "_top_bb"))).createWithSuffix(block, "_working", blockModels.modelOutput);
        blockModels.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(BlockStateProperties.FACING, QpBlockProperty.WORKING)
                    .generate(((direction, working) -> {
                        var model = working ? workingModel : normalModel;
                        var v = Variant.variant().with(VariantProperties.MODEL, model);
                        return rotate(direction, v);
                    })))
        );
        blockModels.registerSimpleItemModel(block, normalModel);
    }

    static VariantProperties.Rotation rotationFromValue(int rot) {
        return switch (rot) {
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> VariantProperties.Rotation.R0;
        };
    }

    void simpleItem(ItemModelGenerators itemModels, QpItem item) {
        itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    void simpleItem(ItemModelGenerators itemModels, QpItem item, String texture) {
        simpleItem(itemModels, item, modLocation(texture));
    }

    void simpleItem(ItemModelGenerators itemModels, QpItem item, ResourceLocation texture) {
        itemModels.itemModelOutput.accept(
            item,
            ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(item, TextureMapping.layer0(texture), itemModels.modelOutput))
        );
    }

    void placer(BlockModelGenerators blockModels) {
        QpBlock block = PlatformAccessNeoForge.RegisterObjectsNeoForge.BLOCK_PLACER.get();
        var model = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(block, new TextureMapping()
                .put(TextureSlot.SIDE, blockTexture("plus_stone_side"))
                .put(TextureSlot.FRONT, blockTexture("placer_front_horizontal"))
                .put(TextureSlot.TOP, blockTexture("placer_front_vertical"))
                .put(TextureSlot.BOTTOM, blockTexture("plus_stone_top"))
            , blockModels.modelOutput
        );
        blockModels.blockStateOutput.accept(
            MultiVariantGenerator.multiVariant(block).with(PropertyDispatch.property(BlockStateProperties.FACING).generate(direction ->
                rotate(direction, Variant.variant().with(VariantProperties.MODEL, model))
            ))
        );
        blockModels.registerSimpleItemModel(block, model);
    }

    void mining_well(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        /*QpBlock block = Holder.BLOCK_MINING_WELL;
        var basePath = block.name.getPath();
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
            var model = blockState.getValue(QpBlockProperty.WORKING) ? workingModel : normalModel;
            var direction = blockState.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder()
                .modelFile(model)
                .rotationX(Math.floorMod(direction.getStepY() * -90, 360))
                .rotationY(direction.getAxis() == Direction.Axis.Y ? 0 : Math.floorMod(((int) direction.toYRot()) + 180, 360))
                .build();
        });
        simpleBlockItem(block, normalModel);*/
    }

    void markers(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        QpBlock markerBlock = PlatformAccess.getAccess().registerObjects().markerBlock().get();
        var model = ExtendedModelTemplateBuilder.builder()
            // Pole
            .element(b -> b.from(7.0f, 0.0f, 7.0f).to(9.0f, 10.0f, 9.0f)
                .allFaces((direction, faceBuilder) -> {
                    if (direction == Direction.UP) {
                        faceBuilder.texture(TextureSlot.TEXTURE).uvs(7.0f, 7.0f, 9.0f, 9.0f);
                    } else if (direction == Direction.DOWN) {
                        faceBuilder.texture(TextureSlot.TEXTURE).uvs(0.0f, 0.0f, 2.0f, 2.0f).cullface(Direction.DOWN);
                    } else {
                        faceBuilder.texture(TextureSlot.TEXTURE).uvs(7.0f, 6.0f, 9.0f, 16.0f);
                    }
                }))
            // North
            .element(b -> b.from(7.0f, 7.0f, 6.0f).to(9.0f, 9.0f, 7.0f)
                .allFaces((direction, faceBuilder) -> {
                    switch (direction.getAxis()) {
                        case X -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(6.0f, 7.0f, 7.0f, 9.0f);
                        case Y -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(7.0f, 6.0f, 9.0f, 7.0f);
                        case Z -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(7.0f, 7.0f, 9.0f, 9.0f);
                    }
                    if (direction == Direction.SOUTH) faceBuilder.cullface(Direction.SOUTH);
                }))
            // South
            .element(b -> b.from(7.0f, 7.0f, 9.0f).to(9.0f, 9.0f, 10.0f)
                .allFaces((direction, faceBuilder) -> {
                    switch (direction.getAxis()) {
                        case X -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(6.0f, 7.0f, 7.0f, 9.0f);
                        case Y -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(7.0f, 6.0f, 9.0f, 7.0f);
                        case Z -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(7.0f, 7.0f, 9.0f, 9.0f);
                    }
                    if (direction == Direction.NORTH) faceBuilder.cullface(Direction.NORTH);
                }))
            // West
            .element(b -> b.from(6.0f, 7.0f, 7.0f).to(7.0f, 9.0f, 9.0f)
                .allFaces((direction, faceBuilder) -> {
                    switch (direction.getAxis()) {
                        case X -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(7.0f, 7.0f, 9.0f, 9.0f);
                        case Y, Z -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(6.0f, 7.0f, 7.0f, 9.0f);
                    }
                    if (direction == Direction.EAST) faceBuilder.cullface(Direction.EAST);
                }))
            // East
            .element(b -> b.from(9.0f, 7.0f, 7.0f).to(10.0f, 9.0f, 9.0f)
                .allFaces((direction, faceBuilder) -> {
                    switch (direction.getAxis()) {
                        case X -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(7.0f, 7.0f, 9.0f, 9.0f);
                        case Y, Z -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(6.0f, 7.0f, 7.0f, 9.0f);
                    }
                    if (direction == Direction.WEST) faceBuilder.cullface(Direction.WEST);
                }))
            .requiredTextureSlot(TextureSlot.TEXTURE)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build()
            .create(modLocation("block/marker_post"), new TextureMapping().put(TextureSlot.TEXTURE, blockTexture(markerBlock.name.getPath())).copySlot(TextureSlot.TEXTURE, TextureSlot.PARTICLE), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(markerBlock, Variant.variant().with(VariantProperties.MODEL, model))
            .with(blockModels.createColumnWithFacing()));

        var itemModel = ModelTemplates.FLAT_ITEM.create(markerBlock.asItem(), TextureMapping.layer0(TextureMapping.getItemTexture(markerBlock.asItem(), "_item")), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(markerBlock.asItem(), ItemModelUtils.plainModel(itemModel));

        for (QpBlock marker : List.<QpBlock>of(PlatformAccess.getAccess().registerObjects().flexibleMarkerBlock().get(), PlatformAccess.getAccess().registerObjects().chunkMarkerBlock().get())) {
            var m = ExtendedModelTemplateBuilder.builder()
                .parent(model)
                .requiredTextureSlot(TextureSlot.TEXTURE)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .build()
                .create(marker, new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(marker)).copySlot(TextureSlot.TEXTURE, TextureSlot.PARTICLE), blockModels.modelOutput);
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(marker, m));

            var i = ModelTemplates.FLAT_ITEM.create(marker.asItem(), TextureMapping.layer0(TextureMapping.getItemTexture(marker.asItem(), "_item")), itemModels.modelOutput);
            itemModels.itemModelOutput.accept(marker.asItem(), ItemModelUtils.plainModel(i));
        }
    }

    void waterloggedMarkers(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        /*models().withExistingParent("block/waterlogged_marker_common", "block/block")
            // Center
            .element()
            .from(6, 4, 6).to(10, 12, 10)
            .allFaces((direction, faceBuilder) -> {
                switch (direction) {
                    case UP, DOWN -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 6, 8, 7);
                    default -> faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 7, 9, 9);
                }
            })
            .end()
            // North
            .element()
            .from(6, 6, 4).to(10, 10, 6)
            .rotation().angle(0).axis(Direction.Axis.Y).origin(8, 8, 8).end()
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.NORTH) {
                    faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 6, 8, 7);
                } else {
                    faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 7, 9, 9);
                }
            })
            .end()
            // South
            .element()
            .from(6, 6, 10).to(10, 10, 12)
            .rotation().angle(0).axis(Direction.Axis.Y).origin(8, 8, 8).end()
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.SOUTH) {
                    faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 6, 8, 7);
                } else {
                    faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 7, 9, 9);
                }
            })
            .end()
            // West
            .element()
            .from(4, 6, 6).to(6, 10, 10)
            .rotation().angle(0).axis(Direction.Axis.Y).origin(8, 8, 8).end()
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.WEST) {
                    faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 6, 8, 7);
                } else {
                    faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 7, 9, 9);
                }
            })
            .end()
            // East
            .element()
            .from(10, 6, 6).to(12, 10, 10)
            .rotation().angle(0).axis(Direction.Axis.Y).origin(8, 8, 8).end()
            .allFaces((direction, faceBuilder) -> {
                if (direction == Direction.EAST) {
                    faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 6, 8, 7);
                } else {
                    faceBuilder.texture(TextureSlot.TEXTURE).uvs(7, 7, 9, 9);
                }
            })
            .end();
        for (QpBlock marker : List.<QpBlock>of()) {
            var baseName = marker.name.getPath();
            var m = models().withExistingParent("block/" + baseName,
                    ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "block/waterlogged_marker_common"))
                .texture("texture", blockTexture(baseName.replace("waterlogged_", "")))
                .texture("particle", blockTexture(baseName.replace("waterlogged_", "")));
            simpleBlock(marker, m);
            simpleBlockItem(marker, m);
            addClientItem(marker.name, marker.name.withPrefix("item/"));
        }*/
    }

    private static String renderTypeName(RenderStateShard type) {
        try {
            var field = RenderStateShard.class.getDeclaredField("name");
            field.setAccessible(true);
            return (String) field.get(type);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
