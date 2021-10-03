package com.yogpc.qp.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QuarryPlus.modID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ConfigCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var parent =
            Commands.literal(QuarryPlus.modID).requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS));
        var configCommand = Commands.literal("config").executes(ConfigCommand::getConfigValues);
        var setCommand = Commands.argument("name", StringArgumentType.word())
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(c -> changeMachineSetting(StringArgumentType.getString(c, "name"), BoolArgumentType.getBool(c, "value"), c)));
        event.getDispatcher().register(parent.then(configCommand.then(setCommand)));
    }

    private static int changeMachineSetting(String name, boolean enabled, CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        if (QuarryPlus.config != null) {
            QuarryPlus.config.enableMap.set(name, enabled);
            commandContext.getSource().sendSuccess(new TextComponent("%s changed to %B".formatted(name, enabled)), true);
            return Command.SINGLE_SUCCESS;
        } else {
            var supplier = new SimpleCommandExceptionType(new TextComponent("QuarryPlus.config is NULL."));
            throw supplier.create();
        }
    }

    private static int getConfigValues(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        if (QuarryPlus.config != null) {
            commandContext.getSource().sendSuccess(new TextComponent(
                String.format("%sQuarryPlus Machine List%s", ChatFormatting.UNDERLINE, ChatFormatting.RESET)
            ), false);
            Holder.conditionHolders().stream()
                .map(Holder.EntryConditionHolder::location)
                .map(ResourceLocation::getPath)
                .sorted()
                .map(l -> String.format("%s%s%s: %B", ChatFormatting.DARK_AQUA, l, ChatFormatting.RESET, QuarryPlus.config.enableMap.enabled(l)))
                .map(TextComponent::new)
                .forEach(c -> commandContext.getSource().sendSuccess(c, false));
            return Command.SINGLE_SUCCESS;
        } else {
            var supplier = new SimpleCommandExceptionType(new TextComponent("QuarryPlus.config is NULL."));
            throw supplier.create();
        }
    }
}
