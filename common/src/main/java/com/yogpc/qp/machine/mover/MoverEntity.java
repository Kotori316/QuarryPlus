package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.QpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class MoverEntity extends BlockEntity {
    final SimpleContainer inventory = new Inventory(2);

    public MoverEntity(BlockPos pos, BlockState blockState) {
        super(PlatformAccess.getAccess().registerObjects().getBlockEntityType((QpBlock) blockState.getBlock()).orElseThrow(),
            pos, blockState);
        inventory.addListener(container -> this.setChanged());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        inventory.fromTag(tag.getList("inventory", Tag.TAG_COMPOUND), registries);
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.createTag(registries));
    }

    private static class Inventory extends SimpleContainer {
        public Inventory(int size) {
            super(size);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> {
                    if (!stack.isEnchanted()) {
                        yield false;
                    }
                    yield switch (stack.getItem()) {
                        case TieredItem tieredItem -> tieredItem.getTier().getUses() >= Tiers.DIAMOND.getUses();
                        case BowItem ignore -> true;
                        default -> false;
                    };
                }
                case 1 -> stack.is(PlatformAccess.getAccess().registerObjects().quarryBlock().get().blockItem);
                default -> false;
            };
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
