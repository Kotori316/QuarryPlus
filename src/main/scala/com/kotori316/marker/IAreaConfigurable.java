package com.kotori316.marker;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import com.kotori316.marker.render.Box;

public interface IAreaConfigurable {
    Runnable setMinMax(BlockPos min, BlockPos max);

    double a = 0.5d, b = 10d / 16d, c = 6d / 16d;

    static Box[] getRenderBox(BlockPos min, BlockPos max) {
        int flag = 0;
        int xMin = min.getX();
        int yMin = min.getY();
        int zMin = min.getZ();
        int xMax = max.getX();
        int yMax = max.getY();
        int zMax = max.getZ();
        if (xMin != xMax)
            flag |= 1;
        if (yMin != yMax)
            flag |= 2;
        if (zMin != zMax)
            flag |= 4;
        AxisAlignedBB[] lineBoxes = new AxisAlignedBB[12];
        if ((flag & 1) == 1) {//x
            lineBoxes[0] = new AxisAlignedBB(xMin + b, yMin + a, zMin + a, xMax + c, yMin + a, zMin + a);
        }
        if ((flag & 2) == 2) {//y
            lineBoxes[4] = new AxisAlignedBB(xMin + a, yMin + b, zMin + a, xMin + a, yMax + c, zMin + a);
        }
        if ((flag & 4) == 4) {//z
            lineBoxes[8] = new AxisAlignedBB(xMin + a, yMin + a, zMin + b, xMin + a, yMin + a, zMax + c);
        }
        if ((flag & 3) == 3) {//xy
            lineBoxes[2] = new AxisAlignedBB(xMin + b, yMax + a, zMin + a, xMax + c, yMax + a, zMin + a);
            lineBoxes[6] = new AxisAlignedBB(xMax + a, yMin + b, zMin + a, xMax + a, yMax + c, zMin + a);
        }
        if ((flag & 5) == 5) {//xz
            lineBoxes[1] = new AxisAlignedBB(xMin + b, yMin + a, zMax + a, xMax + c, yMin + a, zMax + a);
            lineBoxes[9] = new AxisAlignedBB(xMax + a, yMin + a, zMin + b, xMax + a, yMin + a, zMax + c);
        }
        if ((flag & 6) == 6) {//yz
            lineBoxes[5] = new AxisAlignedBB(xMin + a, yMin + b, zMax + a, xMin + a, yMax + c, zMax + a);
            lineBoxes[10] = new AxisAlignedBB(xMin + a, yMax + a, zMin + b, xMin + a, yMax + a, zMax + c);
        }
        if ((flag & 7) == 7) {//xyz
            lineBoxes[3] = new AxisAlignedBB(xMin + b, yMax + a, zMax + a, xMax + c, yMax + a, zMax + a);
            lineBoxes[7] = new AxisAlignedBB(xMax + a, yMin + b, zMax + a, xMax + a, yMax + c, zMax + a);
            lineBoxes[11] = new AxisAlignedBB(xMax + a, yMax + a, zMin + b, xMax + a, yMax + a, zMax + c);
        }

        return Arrays.stream(lineBoxes).filter(Objects::nonNull)
            .map(range -> Box.apply(range, 1d / 8d, 1d / 8d, 1d / 8d, false, false))
            .toArray(Box[]::new);
    }
}
