package com.yogpc.qp.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// @Mod.EventBusSubscriber(modid = QuarryPlus.modID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ConfigCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var configNames = Holder.conditionHolders().stream()
            .map(Holder.EntryConditionHolder::location)
            .map(ResourceLocation::getPath)
            .sorted().toList();
        var parent =
            Commands.literal(QuarryPlus.modID).requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS));
        var configCommand = Commands.literal("config").executes(ConfigCommand::getConfigValues);
        var setCommand =
            Commands.argument("name", new SelectorArgument(configNames))
                .then(Commands.argument("value", BoolArgumentType.bool())
                    .executes(c -> changeMachineSetting(SelectorArgument.getString(c, "name"), BoolArgumentType.getBool(c, "value"), c)));
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

    private record SelectorArgument(List<String> allowedValues) implements ArgumentType<String> {

        @Override
        public String parse(StringReader reader) {
            return reader.readUnquotedString();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return SharedSuggestionProvider.suggest(allowedValues, builder);
        }

        @Override
        public Collection<String> getExamples() {
            return allowedValues;
        }

        static String getString(final CommandContext<?> context, final String name) {
            return context.getArgument(name, String.class);
        }
    }
}
