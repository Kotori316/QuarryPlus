package com.yogpc.qp.machines.quarry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.BreakResult;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.ItemConverter;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

public class TileQuarry extends PowerTile implements BlockEntityClientSerializable, CheckerLog, MachineStorage.HasStorage, EnchantmentLevel.HasEnchantments {
    private static final Marker MARKER = MarkerManager.getMarker("TileQuarry");
    private static final EnchantmentRestriction RESTRICTION = EnchantmentRestriction.builder()
        .add(Enchantments.BLOCK_EFFICIENCY)
        .add(Enchantments.UNBREAKING)
        .add(Enchantments.BLOCK_FORTUNE)
        .add(Enchantments.SILK_TOUCH)
        .build();
    @Nullable
    public Target target;
    public QuarryState state = QuarryState.FINISHED;
    @Nullable
    private Area area;
    // May be unmodifiable
    private List<EnchantmentLevel> enchantments = new ArrayList<>();
    public MachineStorage storage = new MachineStorage();
    public double headX, headY, headZ;
    private boolean bedrockRemove = false;
    public int digMinY = 0;
    private final ItemConverter itemConverter = ItemConverter.defaultConverter();

    public TileQuarry(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.QUARRY_TYPE, pos, state, 10000 * ONE_FE);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        if (target != null)
            nbt.put("target", target.toNbt());
        nbt.putString("state", state.name());
        if (area != null)
            nbt.put("area", area.toNBT());
        {
            var enchantments = new CompoundTag();
            this.enchantments.forEach(e ->
                enchantments.putInt(Objects.requireNonNull(e.enchantmentID(), "Invalid enchantment. " + e.enchantment()).toString(), e.level()));
            nbt.put("enchantments", enchantments);
        }
        nbt.putDouble("headX", headX);
        nbt.putDouble("headY", headY);
        nbt.putDouble("headZ", headZ);
        nbt.put("storage", storage.toNbt());
        nbt.putBoolean("bedrockRemove", bedrockRemove);
        nbt.putInt("digMinY", digMinY);
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        target = nbt.contains("target") ? Target.fromNbt(nbt.getCompound("target")) : null;
        state = QuarryState.valueOf(nbt.getString("state"));
        area = Area.fromNBT(nbt.getCompound("area")).orElse(null);
        {
            var enchantments = nbt.getCompound("enchantments");
            this.enchantments = enchantments.getAllKeys().stream()
                .map(k -> Pair.of(Registry.ENCHANTMENT.get(new ResourceLocation(k)), enchantments.getInt(k)))
                .filter(p -> p.getKey() != null)
                .map(EnchantmentLevel::new)
                .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
                .toList();
        }
        headX = nbt.getDouble("headX");
        headY = nbt.getDouble("headY");
        headZ = nbt.getDouble("headZ");
        storage.readNbt(nbt.getCompound("storage"));
        bedrockRemove = nbt.getBoolean("bedrockRemove");
        digMinY = nbt.getInt("digMinY");
    }

    @Override
    public final CompoundTag toClientTag(CompoundTag tag) {
        if (area != null)
            tag.put("area", area.toNBT());
        tag.putString("state", state.name());
        tag.putDouble("headX", headX);
        tag.putDouble("headY", headY);
        tag.putDouble("headZ", headZ);
        return tag;
    }

    @Override
    public final void fromClientTag(CompoundTag tag) {
        area = Area.fromNBT(tag.getCompound("area")).orElse(null);
        state = QuarryState.valueOf(tag.getString("state"));
        headX = tag.getDouble("headX");
        headY = tag.getDouble("headY");
        headZ = tag.getDouble("headZ");
    }

    public void setArea(@Nullable Area area) {
        this.area = area;
        QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Area changed to {}.", worldPosition, area);
        if (area != null) {
            headX = area.minX();
            headY = area.minY();
            headZ = area.minZ();
        }
    }

    @Nullable
    public Area getArea() {
        return area;
    }

    public void setState(QuarryState quarryState, BlockState blockState) {
        if (this.state != quarryState) {
            this.state = quarryState;
            sync();
            if (level != null) {
                level.setBlock(worldPosition, blockState.setValue(QPBlock.WORKING, quarryState.isWorking), Block.UPDATE_CLIENTS);
                if (!level.isClientSide && !quarryState.isWorking) {
                    logUsage(QuarryPlus.config.common.debug ? QuarryPlus.LOGGER::info : QuarryPlus.LOGGER::debug);
                }
            }
            QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) State changed to {}.", worldPosition, quarryState);
        }
    }

    public void setBedrockRemove(boolean bedrockRemove) {
        this.bedrockRemove = bedrockRemove;
    }

    public ServerLevel getTargetWorld() {
        return (ServerLevel) getLevel();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TileQuarry quarry) {
        if (quarry.hasEnoughEnergy()) {
            // In server world.
            quarry.state.tick(world, pos, state, quarry);
        }
    }

    public BreakResult breakBlock(BlockPos targetPos) {
        return breakBlock(targetPos, true);
    }

    public BreakResult breakBlock(BlockPos targetPos, boolean requireEnergy) {
        var targetWorld = getTargetWorld();
        // Gather Drops
        if (targetPos.getX() % 3 == 0 && targetPos.getZ() % 3 == 0) {
            targetWorld.getEntitiesOfClass(ItemEntity.class, new AABB(targetPos).inflate(5), Predicate.not(i -> i.getItem().isEmpty()))
                .forEach(i -> {
                    storage.addItem(i.getItem());
                    i.kill();
                });
        }
        // Check breakable
        var blockState = targetWorld.getBlockState(targetPos);
        if (blockState.isAir() || !canBreak(targetWorld, targetPos, blockState)) {
            return BreakResult.SKIPPED;
        }
        // Fluid remove
        // Fluid at near
        BreakResult notEnoughEnergy = checkEdgeFluid(targetPos, requireEnergy, targetWorld, this);
        if (notEnoughEnergy == BreakResult.NOT_ENOUGH_ENERGY) return notEnoughEnergy;

        // Fluid at target pos. Maybe not available because Remove Fluid is done before breaking blocks.
        var fluidState = targetWorld.getFluidState(targetPos);
        if (!fluidState.isEmpty() && blockState.getBlock() instanceof BucketPickup fluidBlock) {
            if (requireEnergy && !useEnergy(Constants.getBreakBlockFluidEnergy(this), Reason.REMOVE_FLUID, false)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
            var bucketItem = fluidBlock.pickupBlock(targetWorld, targetPos, blockState);
            this.storage.addFluid(bucketItem);
            blockState = targetWorld.getBlockState(targetPos); // Update reference, because fluid state is changed.
        }

        // Break block
        var hardness = fluidState.isEmpty() ? blockState.getDestroySpeed(targetWorld, targetPos) : 5;
        if (requireEnergy && !useEnergy(Constants.getBreakEnergy(hardness, this), Reason.BREAK_BLOCK, false)) {
            return BreakResult.NOT_ENOUGH_ENERGY;
        }
        var drops = Block.getDrops(blockState, targetWorld, targetPos, targetWorld.getBlockEntity(targetPos), null, getPickaxe());
        drops.stream().map(itemConverter::map).forEach(this.storage::addItem);
        targetWorld.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        var sound = blockState.getSoundType();
        if (requireEnergy)
            targetWorld.playSound(null, targetPos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 4F, sound.getPitch() * 0.8F);

        return BreakResult.SUCCESS;
    }

    public static BreakResult checkEdgeFluid(BlockPos targetPos, boolean requireEnergy, ServerLevel targetWorld, TileQuarry quarry) {
        Area area = quarry.getArea();
        assert area != null;
        boolean flagMinX = targetPos.getX() - 1 == area.minX();
        boolean flagMaxX = targetPos.getX() + 1 == area.maxX();
        boolean flagMinZ = targetPos.getZ() - 1 == area.minZ();
        boolean flagMaxZ = targetPos.getZ() + 1 == area.maxZ();
        if (flagMinX) {
            if (removeFluidAtEdge(targetWorld, new BlockPos(area.minX(), targetPos.getY(), targetPos.getZ()), requireEnergy, quarry)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
        }
        if (flagMaxX) {
            if (removeFluidAtEdge(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), targetPos.getZ()), requireEnergy, quarry)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
        }
        if (flagMinZ) {
            if (removeFluidAtEdge(targetWorld, new BlockPos(targetPos.getX(), targetPos.getY(), area.minZ()), requireEnergy, quarry)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
        }
        if (flagMaxZ) {
            if (removeFluidAtEdge(targetWorld, new BlockPos(targetPos.getX(), targetPos.getY(), area.maxZ()), requireEnergy, quarry)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
        }
        if (flagMinX && flagMinZ) {
            if (removeFluidAtEdge(targetWorld, new BlockPos(area.minX(), targetPos.getY(), area.minZ()), requireEnergy, quarry)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
        }
        if (flagMinX && flagMaxZ) {
            if (removeFluidAtEdge(targetWorld, new BlockPos(area.minX(), targetPos.getY(), area.maxZ()), requireEnergy, quarry)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
        }
        if (flagMaxX && flagMinZ) {
            if (removeFluidAtEdge(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), area.minZ()), requireEnergy, quarry)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
        }
        if (flagMaxX && flagMaxZ) {
            if (removeFluidAtEdge(targetWorld, new BlockPos(area.maxX(), targetPos.getY(), area.maxZ()), requireEnergy, quarry)) {
                return BreakResult.NOT_ENOUGH_ENERGY;
            }
        }
        return BreakResult.SUCCESS;
    }

    /**
     * @return {@code true} if quarry failed to remove fluid due to energy shortage.
     */
    private static boolean removeFluidAtEdge(ServerLevel world, BlockPos pos, boolean requireEnergy, TileQuarry quarry) {
        var state = world.getBlockState(pos);
        var fluidState = world.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            if (state.getBlock() instanceof BucketPickup fluidBlock) {
                if (requireEnergy && !quarry.useEnergy(Constants.getBreakBlockFluidEnergy(quarry), Reason.REMOVE_FLUID, false)) {
                    return true;
                }
                var bucketItem = fluidBlock.pickupBlock(world, pos, state);
                quarry.storage.addFluid(bucketItem);
                if (world.isEmptyBlock(pos) || (fluidBlock instanceof LiquidBlock && !fluidState.isSource())) {
                    world.setBlockAndUpdate(pos, QuarryPlus.ModObjects.BLOCK_FRAME.getDammingState());
                }
            } else if (state.getBlock() instanceof LiquidBlockContainer) {
                float hardness = state.getDestroySpeed(world, pos);
                if (requireEnergy && !quarry.useEnergy(Constants.getBreakEnergy(hardness, quarry), Reason.REMOVE_FLUID, false)) {
                    return true;
                }
                var drops = Block.getDrops(state, world, pos, world.getBlockEntity(pos), null, quarry.getPickaxe());
                drops.forEach(quarry.storage::addItem);
                world.setBlockAndUpdate(pos, QuarryPlus.ModObjects.BLOCK_FRAME.getDammingState());
            }
        }
        return false;
    }

    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = RESTRICTION.filterMap(enchantments).entrySet().stream()
            .map(EnchantmentLevel::new)
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
            .toList();
    }

    public void setTileDataFromItem(@Nullable CompoundTag tileData) {
        if (tileData == null) {
            digMinY = level == null ? 0 : level.getMinBuildHeight();
            return;
        }
        setBedrockRemove(tileData.getBoolean("bedrockRemove"));
        if (tileData.contains("digMinY"))
            digMinY = tileData.getInt("digMinY");
        else
            digMinY = level == null ? 0 : level.getMinBuildHeight();
    }

    public CompoundTag getTileDataForItem() {
        CompoundTag tag = new CompoundTag();
        if (bedrockRemove) tag.putBoolean("bedrockRemove", true);
        if (digMinY != 0) tag.putInt("digMinY", digMinY);
        return tag;
    }

    private ItemStack getPickaxe() {
        ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);
        enchantments.forEach(e -> stack.enchant(e.enchantment(), e.level()));
        return stack;
    }

    double headSpeed() {
        return headSpeed(enchantments.stream()
            .filter(e -> e.enchantment() == Enchantments.BLOCK_EFFICIENCY)
            .mapToInt(EnchantmentLevel::level)
            .findFirst().orElse(0));
    }

    static double headSpeed(int efficiency) {
        if (efficiency >= 4) {
            return Math.pow(2, efficiency - 4);
        } else {
            // 4th root of 8.
            return Math.pow(1.681792830507429, efficiency) / 8;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canBreak(Level targetWorld, BlockPos targetPos, BlockState state) {
        boolean unbreakable = state.getDestroySpeed(targetWorld, targetPos) < 0;
        if (unbreakable && bedrockRemove && state.getBlock() == Blocks.BEDROCK) {
            int worldBottom = targetWorld.getMinBuildHeight();
            if (targetWorld.dimension().equals(Level.NETHER)) {
                return (worldBottom < targetPos.getY() && targetPos.getY() < worldBottom + 5) || (122 < targetPos.getY() && targetPos.getY() < QuarryPlus.config.common.netherTop);
            } else {
                return worldBottom < targetPos.getY() && targetPos.getY() < worldBottom + 5;
            }
        }
        return !unbreakable;
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "%sArea:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, area),
            "%sTarget:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, target),
            "%sState:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, state),
            "%sRemoveBedrock:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, bedrockRemove),
            "%sDigMinY:%s %d".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, digMinY),
            "%sHead:%s (%f, %f, %f)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, headX, headY, headZ),
            "%sEnergy:%s %f FE (%d)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getEnergy())
        ).map(TextComponent::new).toList();
    }

    @Override
    public MachineStorage getStorage() {
        return this.storage;
    }

    @Override
    public List<EnchantmentLevel> getEnchantments() {
        return Collections.unmodifiableList(enchantments);
    }
}
