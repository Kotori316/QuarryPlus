package com.yogpc.qp.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.machines.base.QuarryBlackList.Entry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import org.junit.jupiter.api.Test;
import scala.collection.immutable.Seq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuarryBlackListTest {
    @Test
    void airSerialize() {
        Seq<Entry> example1 = QuarryBlackList.example1();
        Entry air = example1.apply(0);

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
    void vanillaPredicate() {
        String blockName = "minecraft:stone";
        QuarryBlackList.VanillaBlockPredicate p = new QuarryBlackList.VanillaBlockPredicate(blockName);
        JsonElement j = QuarryBlackList.writeEntry(p, JsonOps.INSTANCE);

        QuarryBlackList.Entry loaded = QuarryBlackList.readEntry(new Dynamic<>(JsonOps.INSTANCE, j));
        assertEquals(p, loaded);
    }
}
