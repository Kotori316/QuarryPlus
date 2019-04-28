package com.yogpc.qp.machines.base;

import java.lang.reflect.Constructor;

//import buildcraft.api.mj.IMjConnector;
//import buildcraft.api.mj.IMjReadable;
//import buildcraft.api.mj.IMjReceiver;
//import buildcraft.api.mj.MjAPI;
import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import javax.annotation.Nullable;
import net.minecraftforge.fml.ModList;

//Buildcraft MJ energy api implication

/**
 * Energy Unit is micro MJ (1000000 micro MJ = 1 MJ = 0.1 RF)
 */
/*@net.minecraftforge.fml.common.Optional.Interface(iface = "buildcraft.api.mj.IMjReceiver", modid = QuarryPlus.Optionals.BuildCraft_core)
@net.minecraftforge.fml.common.Optional.Interface(iface = "buildcraft.api.mj.IMjReadable", modid = QuarryPlus.Optionals.BuildCraft_core)*/
public class MjReceiver /*implements IMjReceiver, IMjReadable*/ {
    private final APowerTile tile;

    private static final Constructor<?> CONSTRUCTOR;

    static {
        // TODO change to net.minecraftforge.fml.common.ModAPIManager
        boolean b = ModList.get().isLoaded(QuarryPlus.Optionals.BuildCraft_core);
        @Nullable Constructor<?> temp;
        if (b) {
            try {
                temp = Class.forName("buildcraft.api.mj.MjCapabilityHelper").getConstructor(Class.forName("buildcraft.api.mj.IMjConnector"));
            } catch (ReflectiveOperationException e) {
                if (Config.common().debug()) {
                    QuarryPlus.LOGGER.error(MjReceiver.class.getSimpleName(), e);
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
                return CONSTRUCTOR.newInstance(new MjReceiver(tile));
            } catch (ReflectiveOperationException e) {
                if (Config.common().debug()) {
                    QuarryPlus.LOGGER.error(MjReceiver.class.getSimpleName(), e);
                }
                return null;
            }
        }
    }

    /*@net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public static double getMJFromMicro(long microJoules) {
        return (double) microJoules / MjAPI.MJ;
    }*/

    private MjReceiver(APowerTile tile) {
        this.tile = tile;
    }

   /* @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public long getStored() {
        return tile.getStoredEnergy();
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public long getCapacity() {
        return tile.getMaxStored();
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public long getPowerRequested() {
        return Math.min(tile.maxGot - tile.got, tile.getMaxStored() - tile.getStoredEnergy() - tile.got);
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public long receivePower(long microJoules, boolean simulate) {
        if (tile.canReceive())
            return microJoules - tile.getEnergy(microJoules, !simulate);
        else return microJoules;
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }*/
}
