package com.yogpc.qp.tile

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity

trait HasInventory extends HasInv {
    self: TileEntity =>

    override def isUsableByPlayer(player: EntityPlayer) = self.getWorld.getTileEntity(self.getPos) eq this

}
