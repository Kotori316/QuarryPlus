/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.gui;

import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.container.ContainerAdvQuarry;
import com.yogpc.qp.container.ContainerEnchList;
import com.yogpc.qp.container.ContainerMover;
import com.yogpc.qp.container.ContainerPlacer;
import com.yogpc.qp.container.ContainerWorkbench;
import com.yogpc.qp.tile.TileAdvQuarry;
import com.yogpc.qp.tile.TileBasic;
import com.yogpc.qp.tile.TilePlacer;
import com.yogpc.qp.tile.TileWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler {

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(final int ID, final EntityPlayer p, final World w, final int x, final int y, final int z) {
        BlockPos pos = new BlockPos(x, y, z);
        switch (ID) {
            case QuarryPlusI.guiIdMover:
                return new GuiMover(p, w, x, y, z);
            case QuarryPlusI.guiIdFList:
                return new GuiEnchList(Enchantments.FORTUNE, (TileBasic) w.getTileEntity(pos), p);
            case QuarryPlusI.guiIdSList:
                return new GuiEnchList(Enchantments.SILK_TOUCH, (TileBasic) w.getTileEntity(pos), p);
            case QuarryPlusI.guiIdPlacer:
                return new GuiPlacer(p.inventory, (TilePlacer) w.getTileEntity(pos));
            case QuarryPlusI.guiIdWorkbench:
                return new GuiWorkbench(p.inventory, (TileWorkbench) w.getTileEntity(pos));
            case QuarryPlusI.guiIdAdvQuarry:
                return new GuiAdvQuarry((TileAdvQuarry) w.getTileEntity(pos), p);
        }

        return null;
    }

    @Override
    public Object getServerGuiElement(final int ID, final EntityPlayer p, final World w, final int x, final int y, final int z) {
        BlockPos pos = new BlockPos(x, y, z);
        switch (ID) {
            case QuarryPlusI.guiIdMover:
                return new ContainerMover(p.inventory, w, x, y, z);
            case QuarryPlusI.guiIdFList:
                return new ContainerEnchList((TileBasic) w.getTileEntity(pos), p);
            case QuarryPlusI.guiIdSList:
                return new ContainerEnchList((TileBasic) w.getTileEntity(pos), p);
            case QuarryPlusI.guiIdPlacer:
                return new ContainerPlacer(p.inventory, (TilePlacer) w.getTileEntity(pos));
            case QuarryPlusI.guiIdWorkbench:
                return new ContainerWorkbench(p.inventory, (TileWorkbench) w.getTileEntity(pos));
            case QuarryPlusI.guiIdAdvQuarry:
                return new ContainerAdvQuarry((TileAdvQuarry) w.getTileEntity(pos), p);
        }
        return null;
    }
}
