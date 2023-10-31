package com.yogpc.qp.machines.mini_quarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.*;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MiniQuarryTile extends PowerTile implements CheckerLog,
    EnchantmentLevel.HasEnchantments, MenuProvider, PowerConfig.Provider {
    private List<EnchantmentLevel> enchantments;
    @Nullable
    Area area = null;
    boolean rs;
    final MiniQuarryInventory container = new MiniQuarryInventory();
    @Nullable
    MiniTarget targetIterator;
    Collection<BlockStatePredicate> denyList = defaultBlackList();
    Collection<BlockStatePredicate> allowList = Set.of();

    public MiniQuarryTile(BlockPos pos, BlockState state) {
        super(Holder.MINI_QUARRY_TYPE, pos, state);
        container.addListener(c -> this.setChanged());
    }

    void work() {
        container.getEnergyModule().ifPresent(e -> addEnergy(e.energy() * PowerTile.ONE_FE, false));
        if (!hasEnoughEnergy()) return;
        assert level != null;
        // Interval check
        if (level.getGameTime() % interval(efficiencyLevel()) != 0 || targetIterator == null) return;
        // Energy consumption
        long consumedEnergy = PowerManager.getMiniQuarryEnergy(this);
        if (!useEnergy(consumedEnergy, Reason.MINI_QUARRY, false)) return;
        // Break block
        while (targetIterator.hasNext()) {
            var level = getTargetWorld();
            var pos = targetIterator.next();
            var state = level.getBlockState(pos);
            if (!canBreak(level, pos, state)) {
                TraceQuarryWork.canBreakCheck(this, getBlockPos(), pos, state, "In deny list or unbreakable");
                continue; // The block is in deny list or unbreakable.
            }

            var fakePlayer = QuarryFakePlayer.get(level);
            var event = new BlockEvent.BreakEvent(level, pos, state, fakePlayer);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                TraceQuarryWork.blockRemoveFailed(this, getBlockPos(), pos, state, BreakResult.FAIL_EVENT);
                break; // Denied to break block.
            }

            if (state.getDestroySpeed(level, pos) < 0) {
                // Consume additional energy if quarry tries to remove bedrock.
                var energy = PowerManager.getBreakEnergy(-1, this);
                useEnergy(energy, Reason.BREAK_BLOCK, true);
                consumedEnergy += energy;
            }
            var tools = container.tools();
            var tool = tools.stream().filter(t ->
                t.isCorrectToolForDrops(state)
            ).findFirst().or(() -> tools.stream().filter(t -> {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, t);
                return ForgeHooks.isCorrectToolForDrops(state, fakePlayer);
            }).findFirst());
            final long c = consumedEnergy;
            tool.ifPresent(t -> {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, t);
                var drops = InvUtils.getBlockDrops(state, level, pos, level.getBlockEntity(pos), fakePlayer, t);
                TraceQuarryWork.blockRemoveSucceed(this, getBlockPos(), pos, state, drops, 0, c);
                drops.forEach(this::insertOrDropItem);
                var damage = t.isCorrectToolForDrops(state) ? 1 : 4;
                for (int i = 0; i < damage; i++) {
                    t.mineBlock(level, state, pos, fakePlayer);
                }
                level.removeBlock(pos, false);
                var sound = state.getSoundType();
                level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 4F, sound.getPitch() * 0.8F);
            });
            if (tool.isPresent()) break; // Continue if no tools available
        }
        if (!targetIterator.hasNext()) {
            targetIterator = null;
            finishWork();
        }
    }

    boolean canBreak(Level level, BlockPos pos, BlockState state) {
        if (allowList.stream().anyMatch(t -> t.test(state, level, pos))) {
            return true;
        }
        return state.getDestroySpeed(level, pos) >= 0 &&
            denyList.stream().noneMatch(t -> t.test(state, level, pos));
    }

    ServerLevel getTargetWorld() {
        return (ServerLevel) level;
    }

    boolean isWorking() {
        return targetIterator != null;
    }

    void gotRSPulse() {
        if (isWorking()) {
            finishWork();
        } else {
            startWork();
        }
    }

    public void setArea(@Nullable Area area) {
        this.area = area;
        if (area != null)
            this.targetIterator = MiniTarget.of(area, true);
    }

    void startWork() {
        assert level != null;
        var behind = getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
        Area area;
        if (this.area == null) {
            area = Stream.of(behind, behind.getCounterClockWise(), behind.getClockWise())
                .map(getBlockPos()::relative)
                .flatMap(p -> {
                    if (level.getBlockEntity(p) instanceof QuarryMarker marker) return Stream.of(marker);
                    else return Stream.empty();
                })
                .flatMap(m -> m.getArea().stream().peek(a -> m.removeAndGetItems().forEach(this::insertOrDropItem)))
                .findFirst()
                .map(a -> new Area(a.minX() - 1, a.minY(), a.minZ() - 1, a.maxX() + 1, a.maxY(), a.maxZ() + 1, a.direction()))
                .map(a -> a.aboveY(level.getMinBuildHeight() + 1)) // Do not dig the floor of world.
                .orElse(null);
        } else {
            area = this.area;
        }
        setArea(area);
        if (this.area != null) {
            level.setBlock(getBlockPos(), getBlockState().setValue(QPBlock.WORKING, true), Block.UPDATE_ALL);
            TraceQuarryWork.startWork(this, getBlockPos(), getEnergyStored());
        }
    }

    void finishWork() {
        assert level != null;
        this.targetIterator = null;
        level.setBlock(getBlockPos(), getBlockState().setValue(QPBlock.WORKING, false), Block.UPDATE_ALL);
        TraceQuarryWork.finishWork(this, getBlockPos(), this.getEnergyStored());
        logUsage();
    }

    void insertOrDropItem(ItemStack stack) {
        assert level != null;
        var rest = InvUtils.injectToNearTile(level, getBlockPos(), stack);
        if (rest.isEmpty()) return; // Inserted all items to inventory.
        Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), rest);
    }

    @Override
    public void saveNbtData(CompoundTag nbt) {
        nbt.putBoolean("rs", rs);
        if (area != null)
            nbt.put("area", area.toNBT());
        var enchantments = new CompoundTag();
        this.enchantments.forEach(e ->
            enchantments.putInt(String.valueOf(e.enchantmentID()), e.level()));
        nbt.put("enchantments", enchantments);
        if (targetIterator != null)
            nbt.putLong("current", targetIterator.peek().asLong());
        nbt.put("inventory", container.createTag());
        nbt.put("denyList", denyList.stream().map(BlockStatePredicate::toTag).collect(Collectors.toCollection(ListTag::new)));
        nbt.put("allowList", allowList.stream().map(BlockStatePredicate::toTag).collect(Collectors.toCollection(ListTag::new)));
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        rs = nbt.getBoolean("rs");
        setArea(Area.fromNBT(nbt.getCompound("area")).orElse(null));
        var enchantments = nbt.getCompound("enchantments");
        setEnchantments(enchantments.getAllKeys().stream()
            .mapMulti(MapMulti.getEntry(ForgeRegistries.ENCHANTMENTS, enchantments::getInt))
            .map(EnchantmentLevel::new)
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
            .toList());
        if (nbt.contains("current") && targetIterator != null)
            targetIterator.setCurrent(BlockPos.of(nbt.getLong("current")));
        container.fromTag(nbt.getList("inventory", Tag.TAG_COMPOUND));
        denyList = Stream.concat(nbt.getList("denyList", Tag.TAG_COMPOUND).stream()
            .mapMulti(MapMulti.cast(CompoundTag.class)).map(BlockStatePredicate::fromTag), defaultBlackList().stream()).collect(Collectors.toSet());
        allowList = nbt.getList("allowList", Tag.TAG_COMPOUND).stream()
            .mapMulti(MapMulti.cast(CompoundTag.class)).map(BlockStatePredicate::fromTag).collect(Collectors.toSet());
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "%sArea:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, area),
            "%sTarget:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, Optional.ofNullable(targetIterator).map(MiniTarget::peek).orElse(null)),
            energyString()
        ).map(Component::literal).toList();
    }

    public void setEnchantments(List<EnchantmentLevel> enchantments) {
        this.enchantments = enchantments;
    }

    @Override
    public List<EnchantmentLevel> getEnchantments() {
        return enchantments;
    }

    Container getInv() {
        return container;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MiniQuarryMenu(id, player, getBlockPos());
    }

    static int interval(int efficiency) {
        return switch (efficiency) {
            case 0 -> 40;
            case 1 -> 30;
            case 2 -> 20;
            case 3 -> 10;
            case 4 -> 5;
            case 5 -> 2;
            default -> {
                if (efficiency < 0) yield 100;
                else yield 1;
            }
        };
    }

    static Set<BlockStatePredicate> defaultBlackList() {
        return Set.of(BlockStatePredicate.air(), BlockStatePredicate.fluid());
    }

    static boolean canAddInList(boolean isAllowList, BlockStatePredicate newData) {
        if (isAllowList) {
            return newData != BlockStatePredicate.all() && newData != BlockStatePredicate.air();
        } else {
            return !defaultBlackList().contains(newData);
        }
    }
}
