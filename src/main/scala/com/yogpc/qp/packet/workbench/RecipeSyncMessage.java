package com.yogpc.qp.packet.workbench;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.recipe.CopiedRecipe;
import com.yogpc.qp.recipe.CopiedRecipeSearcher;
import com.yogpc.qp.recipe.RecipeSearcher;
import com.yogpc.qp.tile.TileWorkbench;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To Client only
 */
public class RecipeSyncMessage implements IMessage {
    private Set<CopiedRecipe> recipes;
    private BlockPos pos;
    private int dim;

    public static RecipeSyncMessage create(BlockPos pos, int dim, RecipeSearcher searcher) {
        RecipeSyncMessage message = new RecipeSyncMessage();
        message.recipes = CopiedRecipe.makeCopy(searcher);
        message.pos = pos;
        message.dim = dim;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        this.pos = buffer.readBlockPos();
        this.dim = buffer.readInt();
        this.recipes = IntStream.range(0, buffer.readInt())
            .mapToObj(i -> CopiedRecipe.read(buffer))
            .collect(Collectors.toSet());
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeInt(recipes.size());
        for (CopiedRecipe recipe : recipes) {
            recipe.write(buffer);
        }
    }

    @Override
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileWorkbench) {
                TileWorkbench t = (TileWorkbench) entity;
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() ->
                    t.setSearcher(new CopiedRecipeSearcher(recipes))
                );
            }
        }
        return null;
    }
}
