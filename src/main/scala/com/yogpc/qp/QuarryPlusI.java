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
package com.yogpc.qp;

import com.yogpc.qp.block.BlockAdvPump;
import com.yogpc.qp.block.BlockAdvQuarry;
import com.yogpc.qp.block.BlockBreaker;
import com.yogpc.qp.block.BlockController;
import com.yogpc.qp.block.BlockFrame;
import com.yogpc.qp.block.BlockLaser;
import com.yogpc.qp.block.BlockMarker;
import com.yogpc.qp.block.BlockMiningWell;
import com.yogpc.qp.block.BlockMover;
import com.yogpc.qp.block.BlockPlacer;
import com.yogpc.qp.block.BlockPlainPipe;
import com.yogpc.qp.block.BlockPump;
import com.yogpc.qp.block.BlockQuarry;
import com.yogpc.qp.block.BlockRefinery;
import com.yogpc.qp.block.BlockWorkbench;
import com.yogpc.qp.item.ItemMirror;
import com.yogpc.qp.item.ItemQuarryDebug;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class QuarryPlusI {
    public static final QuarryPlusI INSANCE = new QuarryPlusI();

    private QuarryPlusI() {
    }

    public static final CreativeTabs ct = new CreativeTabQuarryPlus();

    public static final BlockQuarry blockQuarry = new BlockQuarry();
    public static final BlockMarker blockMarker = new BlockMarker();
    public static final BlockMover blockMover = new BlockMover();
    public static final BlockMiningWell blockMiningWell = new BlockMiningWell();
    public static final BlockPump blockPump = new BlockPump();
    public static final BlockRefinery blockRefinery = new BlockRefinery();
    public static final BlockPlacer blockPlacer = new BlockPlacer();
    public static final BlockBreaker blockBreaker = new BlockBreaker();
    public static final BlockLaser blockLaser = new BlockLaser();
    public static final BlockPlainPipe blockPlainPipe = new BlockPlainPipe();
    public static final BlockFrame blockFrame = new BlockFrame();
    public static final BlockWorkbench workbench = new BlockWorkbench();
    public static final BlockController controller = new BlockController();
    public static final BlockAdvQuarry blockChunkdestroyer = new BlockAdvQuarry();
    public static final BlockAdvPump blockStandalonePump = new BlockAdvPump();
    public static final Item itemTool = ItemTool.item();
    public static final Item magicmirror = new ItemMirror();
    public static final Item debugItem = ItemQuarryDebug.item();

    public static final int guiIdWorkbench = 1;
    public static final int guiIdMover = 2;
    public static final int guiIdFList = 3;
    public static final int guiIdSList = 4;
    public static final int guiIdPlacer = 5;
    public static final int guiIdAdvQuarry = 6;

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        final TileMarker.Link[] la = TileMarker.linkList.toArray(new TileMarker.Link[TileMarker.linkList.size()]);
        for (final TileMarker.Link l : la)
            if (l.w == event.getWorld())
                l.removeConnection(false);
        final TileMarker.Laser[] lb = TileMarker.laserList.toArray(new TileMarker.Laser[TileMarker.laserList.size()]);
        for (final TileMarker.Laser l : lb)
            if (l.w == event.getWorld())
                l.destructor();
    }
}
