package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Stream;

public class ExpPumpTile extends BlockEntity implements CheckerLog {
    private final ExpBlockModule module = new ExpBlockModule(this);

    public ExpPumpTile(BlockPos pos, BlockState state) {
        super(Holder.EXP_PUMP_TYPE, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("exp", module.getExp());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.module.setExp(tag.getInt("exp"), true);
    }

    ExpBlockModule getModule() {
        return this.module;
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
                "Exp: " + module.getExp()
        ).map(Component::literal).toList();
    }

    static class ExpBlockModule extends ExpModule {
        private final ExpPumpTile parent;
        private int exp = 0;

        ExpBlockModule(ExpPumpTile parent) {
            this.parent = parent;
        }

        @Override
        public void addExp(int amount) {
            this.exp += amount;
            if (parent.level != null && !parent.getBlockState().getValue(QPBlock.WORKING)) {
                parent.level.setBlock(parent.getBlockPos(), parent.getBlockState().setValue(QPBlock.WORKING, true), Block.UPDATE_ALL);
            }
            parent.setChanged();
        }

        @Override
        public int getExp() {
            return this.exp;
        }

        void setExp(int exp, boolean loading) {
            this.exp = exp;
            if (!loading) {
                if (this.exp <= 0 && parent.level != null && parent.getBlockState().getValue(QPBlock.WORKING)) {
                    parent.level.setBlock(parent.getBlockPos(), parent.getBlockState().setValue(QPBlock.WORKING, false), Block.UPDATE_ALL);
                }
                parent.setChanged();
            }
        }
    }
}
