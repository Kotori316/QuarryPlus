package com.yogpc.qp.machines.base;

import net.minecraft.network.PacketBuffer;

public class TextInClient {
    private final int[] intArray;

    public TextInClient(int... numbers) {
        this.intArray = numbers;
    }

    public static TextInClient create(int... numbers) {
        return new TextInClient(numbers);
    }

    public void write(PacketBuffer buffer) {
        buffer.writeVarIntArray(intArray);
    }

    public static TextInClient read(PacketBuffer buffer) {
        int[] array = buffer.readVarIntArray();
        return new TextInClient(array);
    }

    public int getInt(int index) {
        if (index >= 0 && index < intArray.length) {
            return intArray[index];
        } else {
            return 0;
        }
    }
}
