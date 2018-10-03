package com.yogpc.qp.tile;

import java.lang.reflect.Constructor;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import javax.annotation.Nonnull;
import net.minecraftforge.fml.common.ModAPIManager;

//Buildcraft MJ energy api implecation

/**
 * Energy Unit is micro MJ (1000000 micro MJ = 1 MJ = 0.1 RF)
 */
@net.minecraftforge.fml.common.Optional.InterfaceList({
    @net.minecraftforge.fml.common.Optional.Interface(iface = "buildcraft.api.mj.IMjReceiver", modid = QuarryPlus.Optionals.BuildCraft_core),
    @net.minecraftforge.fml.common.Optional.Interface(iface = "buildcraft.api.mj.IMjReadable", modid = QuarryPlus.Optionals.BuildCraft_core)
})
public class MjReciever implements IMjReceiver, IMjReadable {
    private final APowerTile tile;

    private static final Constructor<?> CONSTRUCTOR;

    static {
        boolean b = ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.BuildCraft_core);
        Constructor<?> temp;
        if (b) {
            try {
                temp = Class.forName("buildcraft.api.mj.MjCapabilityHelper").getConstructor(Class.forName("buildcraft.api.mj.IMjConnector"));
            } catch (ReflectiveOperationException e) {
                if (Config.content().debug()) {
                    QuarryPlus.LOGGER.error(MjReciever.class.getSimpleName(), e);
                }
                temp = null;
            }
        } else {
            temp = null;
        }
        CONSTRUCTOR = temp;
    }

    public static Object mjCapabilityHelper(APowerTile tile) {
        if (CONSTRUCTOR == null) {
            return null;
        } else {
            try {
                return CONSTRUCTOR.newInstance(new MjReciever(tile));
            } catch (ReflectiveOperationException e) {
                if (Config.content().debug()) {
                    QuarryPlus.LOGGER.error(MjReciever.class.getSimpleName(), e);
                }
                return null;
            }
        }
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public static double getMJfrommicro(long microJoules) {
        return (double) microJoules / MjAPI.MJ;
    }

    public MjReciever(APowerTile tile) {
        this.tile = tile;
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public long getStored() {
        return (long) (tile.getStoredEnergy() * MjAPI.MJ);
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public long getCapacity() {
        return (long) (tile.getMaxStored() * MjAPI.MJ);
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public long getPowerRequested() {
        return (long) (Math.min(tile.maxGot - tile.got,
            tile.getMaxStored() - tile.getStoredEnergy() - tile.got) * MjAPI.MJ);
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public long receivePower(long microJoules, boolean simulate) {
        return (long) (microJoules - tile.getEnergy(getMJfrommicro(microJoules), !simulate) * MjAPI.MJ);
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }
}
