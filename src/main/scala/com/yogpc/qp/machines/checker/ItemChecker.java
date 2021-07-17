package com.yogpc.qp.machines.checker;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.CheckerLog;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class ItemChecker extends Item implements UseBlockCallback {
    public static final String NAME = "status_checker";

    public ItemChecker() {
        super(new Settings().group(QuarryPlus.CREATIVE_TAB));
        UseBlockCallback.EVENT.register(this);
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player.isSpectator() || player.getStackInHand(hand).getItem() != this) return ActionResult.PASS;
        if (world.getBlockEntity(hitResult.getBlockPos()) instanceof CheckerLog debug) {
            player.sendMessage(new LiteralText(Formatting.YELLOW + (world.isClient ? "Client" : "Server") + Formatting.RESET), false);
            debug.getDebugLogs().forEach(t -> player.sendMessage(t, false));
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }
}
