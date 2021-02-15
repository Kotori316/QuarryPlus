package com.yogpc.qp.test;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.machines.base.QuarryBlackList.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMaterialMatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockReader;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuarryBlackListTest {
    private static final Predicate<BlockState> NotAir = BlockMaterialMatcher.forMaterial(Material.AIR).negate();

    static Executable entryTest(QuarryBlackList.Entry entry, BlockState state) {
        return () -> assertTrue(entry.test(state, EmptyBlockReader.INSTANCE, BlockPos.ZERO), String.format("Test of %s for %s.", entry, state));
    }

    static Function<BlockState, Executable> getExecutable(QuarryBlackList.Entry entry) {
        return b -> entryTest(entry, b);
    }

    @Test
    void airSerialize() {
        Entry air = QuarryBlackList.Air$.MODULE$;

        INBT n = QuarryBlackList.writeEntry(air, NBTDynamicOps.INSTANCE);
        CompoundNBT tag = QuarryBlackList.Entry$.MODULE$.EntryToNBT().apply(air);

        assertEquals(n, tag);
        assertTrue(tag.contains("id"));
        assertEquals(1, tag.size());

        JsonElement json = QuarryBlackList.writeEntry(air, JsonOps.INSTANCE);
        assertTrue(json.isJsonObject());
        JsonObject object = json.getAsJsonObject();
        assertTrue(object.has("id"));
        assertEquals(1, object.size());
        {
            Entry entry = QuarryBlackList.readEntry(new Dynamic<>(NBTDynamicOps.INSTANCE, n));
            assertEquals(air, entry);
        }
        {
            Entry entry = QuarryBlackList.readEntry(new Dynamic<>(JsonOps.INSTANCE, json));
            assertEquals(air, entry);
        }
    }

    @Test
    void airPredicate() {
        assertAll(Stream.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR)
            .map(Block::getDefaultState)
            .map(getExecutable(QuarryBlackList.Air$.MODULE$)));
    }

    @Test
    void vanillaPredicate() {
        String blockName = "minecraft:stone";
        QuarryBlackList.VanillaBlockPredicate p = new QuarryBlackList.VanillaBlockPredicate(blockName);
        JsonElement j = QuarryBlackList.writeEntry(p, JsonOps.INSTANCE);

        QuarryBlackList.Entry loaded = QuarryBlackList.readEntry(new Dynamic<>(JsonOps.INSTANCE, j));
        assertEquals(p, loaded);
        // We can't test this entry because we don't have way to get world instance.
        // Also we don't have instance of IWorldReader.
    }

    @Test
    void fluidPredicate() {
        QuarryBlackList.Entry entry = QuarryBlackList.Fluids$.MODULE$;
        Stream<Executable> executable = Stream.concat(
            ForgeRegistries.FLUIDS.getValues().stream()
                .map(Fluid::getDefaultState)
                .filter(f -> !f.isEmpty())
                .map(FluidState::getBlockState),
            Stream.of(
                Blocks.WATER.getDefaultState(),
                Blocks.LAVA.getDefaultState()
            )
        ).filter(NotAir).map(getExecutable(entry));

        assertAll(executable);
    }
}
