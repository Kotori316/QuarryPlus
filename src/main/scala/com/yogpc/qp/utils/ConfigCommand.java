package com.yogpc.qp.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.ArrayList;
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
        var checkTicketCommand = Commands.literal("checkTicket").executes(c -> checkTicket(c, BlockPos.containing(c.getSource().getPosition())))
            .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(c -> checkTicket(c, BlockPosArgument.getBlockPos(c, "pos"))));
        event.getDispatcher().register(parent.then(configCommand.then(setCommand)).then(checkTicketCommand));
    }

    private static int changeMachineSetting(String name, boolean enabled, CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        if (QuarryPlus.config != null) {
            QuarryPlus.config.enableMap.set(name, enabled);
            commandContext.getSource().sendSuccess(() -> Component.literal("%s changed to %B".formatted(name, enabled)), true);
            return Command.SINGLE_SUCCESS;
        } else {
            var supplier = new SimpleCommandExceptionType(Component.literal("QuarryPlus.config is NULL."));
            throw supplier.create();
        }
    }

    private static int getConfigValues(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        if (QuarryPlus.config != null) {
            commandContext.getSource().sendSuccess(() -> Component.literal(
                String.format("%sQuarryPlus Machine List%s", ChatFormatting.UNDERLINE, ChatFormatting.RESET)
            ), false);
            Holder.conditionHolders().stream()
                .map(Holder.EntryConditionHolder::location)
                .map(ResourceLocation::getPath)
                .sorted()
                .map(l -> String.format("%s%s%s: %B", ChatFormatting.DARK_AQUA, l, ChatFormatting.RESET, QuarryPlus.config.enableMap.enabled(l)))
                .map(Component::literal)
                .forEach(c -> commandContext.getSource().sendSuccess(() -> c, false));
            return Command.SINGLE_SUCCESS;
        } else {
            var supplier = new SimpleCommandExceptionType(Component.literal("QuarryPlus.config is NULL."));
            throw supplier.create();
        }
    }

    public record SelectorArgument(List<String> allowedValues) implements ArgumentType<String> {

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

    public static final ArgumentTypeInfo<SelectorArgument, ?> INFO = new Info();

    public static final class Info implements ArgumentTypeInfo<SelectorArgument, Template> {

        @Override
        public void serializeToNetwork(ConfigCommand.Template pTemplate, FriendlyByteBuf pBuffer) {
            pBuffer.writeCollection(pTemplate.allowedValues, FriendlyByteBuf::writeUtf);
        }

        @Override
        public ConfigCommand.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
            var list = pBuffer.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
            return new ConfigCommand.Template(list);
        }

        @Override
        public void serializeToJson(ConfigCommand.Template pTemplate, JsonObject pJson) {
            pJson.add("allowedValues",
                pTemplate.allowedValues.stream().map(JsonPrimitive::new).collect(MapMulti.jsonArrayCollector())
            );
        }

        @Override
        public ConfigCommand.Template unpack(SelectorArgument pArgument) {
            return new ConfigCommand.Template(pArgument.allowedValues);
        }
    }

    public record Template(List<String> allowedValues) implements ArgumentTypeInfo.Template<SelectorArgument> {
        @Override
        public SelectorArgument instantiate(CommandBuildContext pContext) {
            return new SelectorArgument(allowedValues);
        }

        @Override
        public ArgumentTypeInfo<SelectorArgument, ?> type() {
            return INFO;
        }
    }

    @SuppressWarnings("unchecked")
    private static int checkTicket(CommandContext<CommandSourceStack> commandContext, BlockPos pos) throws CommandSyntaxException {
        var data = commandContext.getSource();
        var distanceManager = data.getLevel().getChunkSource().chunkMap.getDistanceManager();
        var chunkPos = new ChunkPos(pos);
        try {
            var method = ObfuscationReflectionHelper.findMethod(DistanceManager.class, "m_140857_", long.class);
            method.setAccessible(true);
            var tickets = (SortedArraySet<Ticket<?>>) method.invoke(distanceManager, chunkPos.toLong());

            QuarryPlus.LOGGER.info("Tickets for chunk {}({}) is {}", chunkPos, pos, tickets);
            data.sendSystemMessage(Component.literal("Tickets for chunk %s(%s) is %s".formatted(chunkPos, pos, tickets)));
        } catch (ReflectiveOperationException e) {
            QuarryPlus.LOGGER.error("Can't access to getTickets method.", e);
            var supplier = new SimpleCommandExceptionType(Component.literal(e.getMessage()));
            throw supplier.create();
        }

        return Command.SINGLE_SUCCESS;
    }
}
