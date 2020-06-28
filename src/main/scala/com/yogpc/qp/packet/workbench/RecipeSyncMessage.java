package com.yogpc.qp.packet.workbench;

import java.util.function.Supplier;

import com.yogpc.qp.machines.workbench.WorkbenchRecipes;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.jdk.javaapi.CollectionConverters;

/**
 * From Dedicated Server to Client.
 *
 * @deprecated This packet is no longer used.
 */
@Deprecated
public class RecipeSyncMessage implements IMessage<RecipeSyncMessage> {

    private scala.collection.Map<ResourceLocation, WorkbenchRecipes> recipesMap;

    @Deprecated
    public static RecipeSyncMessage create(scala.collection.Map<ResourceLocation, WorkbenchRecipes> recipesMap) {
        RecipeSyncMessage message = new RecipeSyncMessage();
        message.recipesMap = recipesMap;
        return message;
    }

    @Override
    public RecipeSyncMessage readFromBuffer(PacketBuffer buffer) {
        int size = buffer.readInt();
        java.util.Map<ResourceLocation, WorkbenchRecipes> map = new java.util.HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation location = buffer.readResourceLocation();
            WorkbenchRecipes read = WorkbenchRecipes.Serializer$.MODULE$.read(location, buffer);
            map.put(location, read);
        }
        recipesMap = CollectionConverters.asScala(map);
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeInt(this.recipesMap.size());
        CollectionConverters.asJava(this.recipesMap).forEach((l, workbenchRecipes) -> {
                buffer.writeResourceLocation(l);
                WorkbenchRecipes.Serializer$.MODULE$.write(buffer, workbenchRecipes);
            }
        );
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        // Packet going to be removed.
    }

}
