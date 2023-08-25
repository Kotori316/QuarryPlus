/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.machines.mover;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

public class BlockMover extends QPBlock {
    public static final String NAME = "mover";
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + NAME;

    public BlockMover() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK).strength(1.2f), NAME, BlockMoverItem::new);
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
                NetworkHooks.openScreen((ServerPlayer) player, new InteractionObject(pos, this.getName()), pos);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    private record InteractionObject(BlockPos pos, Component name) implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return name;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new ContainerMover(id, player, pos);
        }
    }

    private static final class BlockMoverItem extends QPBlock.QPBlockItem {

        public BlockMoverItem(QPBlock block) {
            super(block, new Item.Properties());
        }

        @Override
        public List<ItemStack> creativeTabItem() {
            List<ItemStack> stacks = new ArrayList<>();
            stacks.add(new ItemStack(this));
            {
                var stack = new ItemStack(Items.DIAMOND_PICKAXE);
                stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
                stack.enchant(Enchantments.UNBREAKING, 3);
                stack.enchant(Enchantments.BLOCK_FORTUNE, 3);
                stacks.add(stack);
            }
            {
                var stack = new ItemStack(Items.DIAMOND_PICKAXE);
                stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
                stack.enchant(Enchantments.UNBREAKING, 3);
                stack.enchant(Enchantments.SILK_TOUCH, 1);
                stacks.add(stack);
            }
            return stacks;
        }
    }
}
