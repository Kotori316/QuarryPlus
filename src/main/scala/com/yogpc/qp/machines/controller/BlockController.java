package com.yogpc.qp.machines.controller;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.QuarryFakePlayer;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.MapMulti;
import cpw.mods.modlauncher.Launcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockController extends QPBlock {
    public static final String NAME = "spawner_controller";
    private static final Logger LOGGER = QuarryPlus.getLogger(BlockController.class);
    private static final Field logic_spawnDelay = getSpawnDelayField(); // int
    private static final Field logic_nextSpawnData = getNextSpawnDataField(); // (Level, BlockPos) -> ResourceLocation

    public BlockController() {
        super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak()
            .strength(1.0f), NAME);
        registerDefaultState(getStateDefinition().any().setValue(WORKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    private static Optional<Pair<BaseSpawner, BlockPos>> getSpawner(Level level, BlockPos pos) {
        return Stream.of(Direction.values()).map(pos::relative).map(level::getBlockEntity)
            .mapMulti(MapMulti.cast(SpawnerBlockEntity.class))
            .map(s -> Pair.of(s.getSpawner(), s.getBlockPos())).findFirst();
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!QuarryPlus.config.enableMap.enabled(NAME)) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                if (player.getItemInHand(hand).is(Holder.ITEM_CHECKER)) {
                    // Tell spawner info
                    getSpawner(level, pos)
                        .map(Pair::getLeft)
                        .flatMap(BlockController::getEntityId)
                        .map(ResourceLocation::toString)
                        .map(s -> "Spawner Mob: " + s)
                        .map(Component::literal)
                        .ifPresent(s -> player.displayClientMessage(s, false));
                } else {
                    // Open GUI
                    var entries = ForgeRegistries.ENTITY_TYPES.getValues().stream()
                        .filter(BlockController::canSpawnFromSpawner)
                        .map(ForgeRegistries.ENTITY_TYPES::getKey)
                        .collect(Collectors.toList());
                    PacketHandler.sendToClientPlayer(new ControllerOpenMessage(pos, level.dimension(), entries), (ServerPlayer) player);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static Optional<ResourceLocation> getEntityId(BaseSpawner spawner) {
        try {
            return Optional.ofNullable(logic_nextSpawnData.get(spawner))
                .flatMap(MapMulti.optCast(SpawnData.class))
                .map(SpawnData::getEntityToSpawn)
                .flatMap(EntityType::by)
                .map(ForgeRegistries.ENTITY_TYPES::getKey);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Exception occurred in getting entity id.", e);
            return Optional.empty();
        }
    }

    private static boolean canSpawnFromSpawner(EntityType<?> entityType) {
        if (QuarryPlus.config == null) return true;
        else if (!entityType.canSummon()) return false;
        return !QuarryPlus.config.common.spawnerBlackList.get().contains(String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(entityType)));
    }

    public static void setSpawnerEntity(Level world, BlockPos pos, ResourceLocation name) {
        getSpawner(world, pos).ifPresent(logic -> {
            Optional.of(name)
                .map(ForgeRegistries.ENTITY_TYPES::getValue)
                .filter(BlockController::canSpawnFromSpawner)
                .ifPresent(entityType -> logic.getLeft().setEntityId(entityType, world, world.getRandom(), pos));
            Optional.ofNullable(logic.getLeft().getSpawnerBlockEntity()).ifPresent(BlockEntity::setChanged);
            BlockState state = world.getBlockState(logic.getRight());
            world.sendBlockUpdated(logic.getRight(), state, state, Block.UPDATE_INVISIBLE);
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
                        logic_spawnDelay.setInt(logic.getLeft(), 0);
                    } catch (IllegalAccessException e) {
                        LOGGER.warn(e);
                        return;
                    }
                    ServerPlayer fakePlayer = QuarryFakePlayer.get((ServerLevel) level);
                    fakePlayer.setPos(logic.getRight().getX(), logic.getRight().getY(), logic.getRight().getZ());
//                    logic.getWorld().players.add(fakePlayer);
                    logic.getLeft().serverTick((ServerLevel) level, logic.getRight());
//                    logic.getWorld().players.remove(fakePlayer);
                });
            }
            level.setBlock(pos, state.setValue(QPBlock.WORKING, powered), Block.UPDATE_ALL);
        }
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);
    }

    private static Field getSpawnDelayField() {
        if (Launcher.INSTANCE != null) {
            return ObfuscationReflectionHelper.findField(BaseSpawner.class, "f_45442_");
        } else {
            try {
                var f = BaseSpawner.class.getDeclaredField("spawnDelay");
                f.setAccessible(true);
                return f;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Field getNextSpawnDataField() {
        if (Launcher.INSTANCE != null) {
            return ObfuscationReflectionHelper.findField(BaseSpawner.class, "f_45444_");
        } else {
            try {
                var f = BaseSpawner.class.getDeclaredField("nextSpawnData");
                f.setAccessible(true);
                return f;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
