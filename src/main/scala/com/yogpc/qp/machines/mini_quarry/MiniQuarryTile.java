package com.yogpc.qp.machines.mini_quarry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.InvUtils;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.QuarryFakePlayer;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.utils.MapMulti;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class MiniQuarryTile extends PowerTile implements CheckerLog,
    EnchantmentLevel.HasEnchantments, MenuProvider {
    private List<EnchantmentLevel> enchantments;
    @Nullable
    Area area = null;
    boolean rs;
    MiniQuarryInventory container = new MiniQuarryInventory();
    @Nullable
    MiniTarget targetIterator;

    public MiniQuarryTile(BlockPos pos, BlockState state) {
        super(Holder.MINI_QUARRY_TYPE, pos, state);
        container.addListener(c -> this.setChanged());
    }

    void work() {
        container.getEnergyModule().ifPresent(e -> addEnergy(e.energy(), false));
        if (!hasEnoughEnergy()) return;
        assert level != null;
        // Interval check
        if (level.getGameTime() % interval(efficiencyLevel()) != 0 || targetIterator == null) return;
        // Energy consumption
        if (!useEnergy(PowerManager.getMiniQuarryEnergy(this), Reason.MINI_QUARRY, false)) return;
        var tools = container.tools();
        // Break block
        while (targetIterator.hasNext()) {
            var level = getTargetWorld();
            var pos = targetIterator.next();
            var state = level.getBlockState(pos);
            if (!canBreak(level, pos, state)) continue; // The block is in deny list or unbreakable.

            var fakePlayer = QuarryFakePlayer.get(level);
            var event = new BlockEvent.BreakEvent(level, pos, state, fakePlayer);
            if (MinecraftForge.EVENT_BUS.post(event)) break; // Denied to break block.

            var tool = tools.stream().filter(t -> {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, t);
                return fakePlayer.hasCorrectToolForDrops(state);
            }).findFirst().or(() -> tools.stream().filter(t -> {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, t);
                return ForgeHooks.isCorrectToolForDrops(state, fakePlayer);
            }).findFirst());
            tool.ifPresent(t -> {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, t);
                var drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), fakePlayer, t);
                drops.forEach(this::insertOrDropItem);
                var damage = fakePlayer.hasCorrectToolForDrops(state) ? 1 : 4;
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
            area = null;
            targetIterator = null;
            finishWork();
        }
    }

    boolean canBreak(Level level, BlockPos pos, BlockState state) {
        return !state.isAir() &&
            state.getDestroySpeed(level, pos) >= 0;
    }

    ServerLevel getTargetWorld() {
        return (ServerLevel) level;
    }

    boolean isWorking() {
        return area != null;
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
            this.targetIterator = new MiniTarget(area);
    }

    void startWork() {
        assert level != null;
        var behind = getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
        setArea(Stream.of(behind, behind.getCounterClockWise(), behind.getClockWise())
            .map(getBlockPos()::relative)
            .flatMap(p -> {
                if (level.getBlockEntity(p) instanceof QuarryMarker marker) return Stream.of(marker);
                else return Stream.empty();
            })
            .flatMap(m -> m.getArea().stream().peek(a -> m.removeAndGetItems().forEach(this::insertOrDropItem)))
            .findFirst()
            .map(a -> new Area(a.minX() - 1, a.minY(), a.minZ() - 1, a.maxX() + 1, a.maxY(), a.maxZ() + 1, a.direction()))
            .orElse(null));
        if (area != null)
            level.setBlock(getBlockPos(), getBlockState().setValue(QPBlock.WORKING, true), Block.UPDATE_ALL);
    }

    void finishWork() {
        assert level != null;
        level.setBlock(getBlockPos(), getBlockState().setValue(QPBlock.WORKING, false), Block.UPDATE_ALL);
        logUsage();
    }

    void insertOrDropItem(ItemStack stack) {
        assert level != null;
        var rest = InvUtils.injectToNearTile(level, getBlockPos(), stack);
        if (rest.isEmpty()) return; // Inserted all items to inventory.
        Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), rest);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
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
        return super.save(nbt);
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
        container.fromTag(nbt.getList("inventory", Constants.NBT.TAG_COMPOUND));
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "%sArea:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, area),
            "%sTarget:%s %s".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, Optional.ofNullable(targetIterator).map(MiniTarget::peek).orElse(null)),
            "%sEnergy:%s %f/%d FE (%d)".formatted(ChatFormatting.GREEN, ChatFormatting.RESET, getEnergy() / (double) PowerTile.ONE_FE, getMaxEnergyStored(), getEnergy())
        ).map(TextComponent::new).toList();
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
}
