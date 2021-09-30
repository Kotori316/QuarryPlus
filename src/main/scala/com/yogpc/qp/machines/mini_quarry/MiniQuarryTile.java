package com.yogpc.qp.machines.mini_quarry;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class MiniQuarryTile extends PowerTile implements CheckerLog,
    EnchantmentLevel.HasEnchantments, MenuProvider {
    private List<EnchantmentLevel> enchantments;
    Area area = new Area(BlockPos.ZERO, BlockPos.ZERO, Direction.NORTH);
    boolean rs;

    public MiniQuarryTile(BlockPos pos, BlockState state) {
        super(Holder.MINI_QUARRY_TYPE, pos, state);
    }

    void work() {
        assert level != null;
    }

    void gotRSPulse() {

    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putBoolean("rs", rs);
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
        area = Area.fromNBT(nbt.getCompound("area")).orElseGet(() -> new Area(BlockPos.ZERO, BlockPos.ZERO, Direction.NORTH));
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
        return null;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return null;
    }
}
