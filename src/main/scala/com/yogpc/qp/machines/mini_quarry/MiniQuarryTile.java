package com.yogpc.qp.machines.mini_quarry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

public final class MiniQuarryTile extends PowerTile implements CheckerLog,
    EnchantmentLevel.HasEnchantments, MenuProvider {
    private List<EnchantmentLevel> enchantments;
    Area area = null;
    boolean rs;
    SimpleContainer container = new SimpleContainer(5);

    public MiniQuarryTile(BlockPos pos, BlockState state) {
        super(Holder.MINI_QUARRY_TYPE, pos, state);
        container.addListener(c -> this.setChanged());
    }

    void work() {
        assert level != null;
        if (level.getGameTime() % interval(efficiencyLevel()) == 0) {
            if (useEnergy(PowerManager.getMiniQuarryEnergy(this), Reason.MINI_QUARRY, false)) {

            }
        }
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

    void startWork() {
        assert level != null;
        var facing = getBlockState().getValue(BlockStateProperties.FACING);
        area = Stream.of(facing, facing.getCounterClockWise(), facing.getClockWise())
            .map(getBlockPos()::relative)
            .flatMap(p -> {
                if (level.getBlockEntity(p) instanceof QuarryMarker marker) return Stream.of(marker);
                else return Stream.empty();
            })
            .flatMap(m -> m.getArea().stream().peek(a -> m.removeAndGetItems().forEach(this::insertOrDropItem)))
            .findFirst().orElse(null);
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
        for (var direction : Direction.values()) {
            var dest = Optional.ofNullable(level.getBlockEntity(getBlockPos().relative(direction)))
                .flatMap(e -> e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).resolve());
            var rest = dest.map(h -> ItemHandlerHelper.insertItem(h, stack, false)).orElse(stack);
            if (rest.isEmpty()) return; // Inserted all items to inventory.
            Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), rest);
        }
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
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        rs = nbt.getBoolean("rs");
        area = Area.fromNBT(nbt.getCompound("area")).orElse(null);
        var enchantments = nbt.getCompound("enchantments");
        setEnchantments(enchantments.getAllKeys().stream()
            .mapMulti(MapMulti.getEntry(ForgeRegistries.ENCHANTMENTS, enchantments::getInt))
            .map(EnchantmentLevel::new)
            .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
            .toList());
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return null;
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
        return null;
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
