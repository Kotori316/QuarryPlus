package com.yogpc.qp;

import com.mojang.serialization.Codec;
import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public final class QuarryDataComponents {
    public static final DataComponentType<Boolean> QUARRY_REMOVE_BEDROCK_COMPONENT = DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
    public static final DataComponentType<Integer> HOLDING_EXP_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build();
    public static final DataComponentType<Integer> FILTER_MODULE_ROWS_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build();
    public static final DataComponentType<List<MachineStorage.ItemKey>> ITEM_KEY_LIST_COMPONENT = DataComponentType.<List<MachineStorage.ItemKey>>builder()
        .persistent(MachineStorage.ITEM_KEY_MAP_CODEC.codec().listOf())
        .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(MachineStorage.ITEM_KEY_MAP_CODEC.codec().listOf()))
        .build();

    public static final Map<Identifier, DataComponentType<?>> ALL = Map.of(
        Identifier.fromNamespaceAndPath(QuarryPlus.modID, "quarry_remove_bedrock_component"), QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT,
        Identifier.fromNamespaceAndPath(QuarryPlus.modID, "quarry_holding_exp_component"), QuarryDataComponents.HOLDING_EXP_COMPONENT,
        Identifier.fromNamespaceAndPath(QuarryPlus.modID, "filter_module_rows"), QuarryDataComponents.FILTER_MODULE_ROWS_COMPONENT,
        Identifier.fromNamespaceAndPath(QuarryPlus.modID, "quarry_item_key_list"), QuarryDataComponents.ITEM_KEY_LIST_COMPONENT
    );
}
