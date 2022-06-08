package com.yogpc.qp.machines.mini_quarry;

import java.util.Locale;
import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

interface BlockStatePredicate {
    boolean test(BlockState state, BlockGetter level, BlockPos pos);

    static BlockStatePredicate air() {
        return Air.INSTANCE;
    }

    static BlockStatePredicate fluid() {
        return Fluid.INSTANCE;
    }

    static BlockStatePredicate name(ResourceLocation location) {
        return new Name(location);
    }

    static BlockStatePredicate tag(ResourceLocation location) {
        return new Tag(location);
    }

    static BlockStatePredicate predicateString(String blockPredicate) {
        return new VanillaBlockPredicate(blockPredicate);
    }

    static BlockStatePredicate all() {
        return All.INSTANCE;
    }

    /**
     * Create predicate from nbt tag. Tag must contain "type" to specify type of the predicate.
     * Also, it should contain property for each predicate, such as block name and tag name.
     */
    static BlockStatePredicate fromTag(CompoundTag tag) {
        var type = tag.getString("type");
        return switch (type) {
            case "all" -> all();
            case "air" -> air();
            case "fluid" -> fluid();
            case "name" -> name(new ResourceLocation(tag.getString("location")));
            case "tag" -> tag(new ResourceLocation(tag.getString("location")));
            case "vanilla" -> predicateString(tag.getString("predicate"));
            default -> throw new IllegalArgumentException("invalid type name: %s, got from %s".formatted(type, tag));
        };
    }

    CompoundTag toTag();

    final class All implements BlockStatePredicate {
        private static final All INSTANCE = new All();

        private All() {
        }

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return true;
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", toString().toLowerCase(Locale.ROOT));
            return tag;
        }

        @Override
        public String toString() {
            return "All";
        }
    }

    final class Air implements BlockStatePredicate {
        private static final Air INSTANCE = new Air();

        private Air() {
        }

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return state.isAir();
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", toString().toLowerCase(Locale.ROOT));
            return tag;
        }

        @Override
        public String toString() {
            return "Air";
        }
    }

    final class Fluid implements BlockStatePredicate {
        private static final Fluid INSTANCE = new Fluid();

        private Fluid() {
        }

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return !state.getFluidState().isEmpty();
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", toString().toLowerCase(Locale.ROOT));
            return tag;
        }

        @Override
        public String toString() {
            return "Fluid";
        }
    }

    record Name(ResourceLocation location) implements BlockStatePredicate {

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return location.equals(ForgeRegistries.BLOCKS.getKey(state.getBlock()));
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", "name");
            tag.putString("location", location.toString());
            return tag;
        }

        @Override
        public String toString() {
            return "Name{" +
                   "location=" + location +
                   '}';
        }
    }

    final class Tag implements BlockStatePredicate {
        private final TagKey<Block> tag;
        private final ResourceLocation location;

        private Tag(ResourceLocation tagName) {
            this.location = tagName;
            this.tag = TagKey.create(Registry.BLOCK_REGISTRY, tagName);
        }

        @Override
        public boolean test(BlockState state, BlockGetter level, BlockPos pos) {
            return state.is(tag);
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", "tag");
            tag.putString("location", location.toString());
            return tag;
        }

        @Override
        public String toString() {
            return "Tag{" + location + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tag tag = (Tag) o;

            return location.equals(tag.location);
        }

        @Override
        public int hashCode() {
            return location.hashCode();
        }
    }

    final class VanillaBlockPredicate implements BlockStatePredicate {
        private static final Logger LOGGER = QuarryPlus.getLogger(VanillaBlockPredicate.class);
        private final String blockPredicate;

        public VanillaBlockPredicate(String blockPredicate) {
            this.blockPredicate = blockPredicate;
        }

        @Override
        public boolean test(BlockState state, BlockGetter blockGetter, BlockPos pos) {
            if (blockGetter instanceof Level level) {
                try {
                    BlockPredicateArgument.Result argument = BlockPredicateArgument.blockPredicate(new CommandBuildContext(
                            Optional.ofNullable(level.getServer()).map(MinecraftServer::registryAccess).orElseThrow()))
                        .parse(new StringReader(blockPredicate));
                    return argument.test(new BlockInWorld(level, pos, true));
                } catch (CommandSyntaxException e) {
                    LOGGER.warn("Caught error in creating predicate.", e);
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public CompoundTag toTag() {
            var tag = new CompoundTag();
            tag.putString("type", "vanilla");
            tag.putString("predicate", blockPredicate);
            return tag;
        }

        @Override
        public String toString() {
            return "VanillaBlockPredicate{" +
                   "blockPredicate='" + blockPredicate + '\'' +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VanillaBlockPredicate that = (VanillaBlockPredicate) o;

            return blockPredicate.equals(that.blockPredicate);
        }

        @Override
        public int hashCode() {
            return blockPredicate.hashCode();
        }
    }
}
