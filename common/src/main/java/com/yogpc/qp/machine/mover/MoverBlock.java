package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpBlockItem;
import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

import java.util.stream.Stream;

public final class MoverBlock extends QpEntityBlock {
    public static final String NAME = "mover";

    public MoverBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK).strength(1.2f), NAME, b -> new QpBlockItem(b, new Item.Properties()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MoverEntity entity) {
            if (!level.isClientSide()) {
                if (entity.enabled) {
                    PlatformAccess.getAccess().openGui((ServerPlayer) player, new GeneralScreenHandler<>(entity, MoverContainer::new));
                } else {
                    player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Logic from Containers.dropContentsOnDestroy()
            if (level.getBlockEntity(pos) instanceof MoverEntity entity) {
                Containers.dropContents(level, pos, entity.inventory);
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Stream<ItemStack> creativeTabItem(CreativeModeTab.ItemDisplayParameters parameters) {
        var enchantments = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);
        var builder = Stream.<ItemStack>builder();
        {
            var stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.enchant(enchantments.getOrThrow(Enchantments.EFFICIENCY), 5);
            stack.enchant(enchantments.getOrThrow(QuarryPickaxeEnchantment.KEY), 1);
            builder.add(stack);
        }
        {
            var stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.enchant(enchantments.getOrThrow(Enchantments.EFFICIENCY), 5);
            stack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 3);
            stack.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), 3);
            builder.add(stack);
        }
        {
            var stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.enchant(enchantments.getOrThrow(Enchantments.EFFICIENCY), 5);
            stack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 3);
            stack.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
            builder.add(stack);
        }
        return Stream.concat(
            super.creativeTabItem(parameters),
            builder.build()
        );
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new MoverBlock();
    }
}
