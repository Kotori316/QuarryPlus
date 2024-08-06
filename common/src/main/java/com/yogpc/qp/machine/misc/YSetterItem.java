package com.yogpc.qp.machine.misc;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.GeneralScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class YSetterItem extends Item implements InCreativeTabs {
    public static final String NAME = "y_setter";
    public final ResourceLocation name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME);

    public YSetterItem() {
        super(new Properties());
    }

    protected final InteractionResult interact(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player) {
        var entity = level.getBlockEntity(pos);
        var accessor = YAccessor.get(entity);
        if (entity == null || accessor == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            // accessor.entity().syncToClient();
            PlatformAccess.getAccess().openGui((ServerPlayer) player, new GeneralScreenHandler<>(entity, YSetterContainer::new));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
