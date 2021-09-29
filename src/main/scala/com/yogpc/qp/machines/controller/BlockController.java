package com.yogpc.qp.machines.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.MapMulti;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockController extends QPBlock {
    public static final String NAME = "spawner_controller";
    private static final Logger LOGGER = LogManager.getLogger(BlockController.class);
    private static final Field logic_spawnDelay; //
    private static final Method logic_getEntityID; // (Level, BlockPos) -> ResourceLocation

    static {
        logic_spawnDelay = ObfuscationReflectionHelper.findField(BaseSpawner.class, "f_45442_");
        logic_getEntityID = ObfuscationReflectionHelper.findMethod(BaseSpawner.class, "m_151332_", Level.class, BlockPos.class);
    }

    public BlockController() {
        super(Properties.of(Material.DECORATION)
            .strength(1.0f), NAME);
        registerDefaultState(getStateDefinition().any().setValue(WORKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    private static Optional<BaseSpawner> getSpawner(Level level, BlockPos pos) {
        return Stream.of(Direction.values()).map(pos::relative).map(level::getBlockEntity)
            .mapMulti(MapMulti.cast(SpawnerBlockEntity.class))
            .map(SpawnerBlockEntity::getSpawner).findFirst();
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!QuarryPlus.config.enableMap.enabled(NAME)) {
            if (!level.isClientSide)
                player.displayClientMessage(new TranslatableComponent("quarryplus.chat.disable_message", getName()), false);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                if (player.getItemInHand(hand).is(Holder.ITEM_CHECKER)) {
                    // Tell spawner info
                    getSpawner(level, pos)
                        .flatMap(logic -> getEntityId(logic, level, pos))
                        .map(ResourceLocation::toString)
                        .map(s -> "Spawner Mob: " + s)
                        .map(TextComponent::new)
                        .ifPresent(s -> player.displayClientMessage(s, false));
                } else {
                    // Open GUI
                    List<EntityType<?>> entries = ForgeRegistries.ENTITIES.getValues().stream()
                        .filter(BlockController::canSpawnFromSpawner)
                        .collect(Collectors.toList());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static Optional<ResourceLocation> getEntityId(BaseSpawner spawner, @Nullable Level level, BlockPos pos) {
        try {
            return Optional.ofNullable(logic_getEntityID.invoke(spawner, level, pos))
                .flatMap(MapMulti.optCast(ResourceLocation.class));
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Exception occurred in getting entity id.", e);
            return Optional.empty();
        }
    }

    private static boolean canSpawnFromSpawner(EntityType<?> entityType) {
        return true;
    }

    public static void setSpawnerEntity(Level world, BlockPos pos, ResourceLocation name) {
        getSpawner(world, pos).ifPresent(logic -> {
            Optional.of(name)
                .map(ForgeRegistries.ENTITIES::getValue)
                .filter(BlockController::canSpawnFromSpawner)
                .ifPresent(logic::setEntityId);
            Optional.ofNullable(logic.getSpawnerBlockEntity()).ifPresent(BlockEntity::setChanged);
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, Constants.BlockFlags.DEFAULT);
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && QuarryPlus.config.enableMap.enabled(NAME)) {
            boolean powered = level.hasNeighborSignal(pos);
            boolean m = state.getValue(QPBlock.WORKING);
            if (powered && !m) {
                getSpawner(level, pos).ifPresent(logic -> {
                    try {
                        logic_spawnDelay.setInt(logic, 0);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return;
                    }
                    FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((ServerLevel) level);
                    fakePlayer.setLevel((ServerLevel) level);
                    fakePlayer.setPos(pos.getX(), pos.getY(), pos.getZ());
//                    logic.getWorld().players.add(fakePlayer);
                    logic.serverTick((ServerLevel) level, pos);
//                    logic.getWorld().players.remove(fakePlayer);
                });
            }
            level.setBlock(pos, state.setValue(QPBlock.WORKING, powered), Block.UPDATE_ALL);
        }
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);
    }
}
