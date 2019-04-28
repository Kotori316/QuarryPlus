package com.yogpc.qp.render

import com.yogpc.qp.QuarryPlus
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent

import scala.collection.mutable

object Sprites {

  val instance = this
  private[this] val map = mutable.Map.empty[Symbol, TextureAtlasSprite]
  private[this] final val symbols = List('laser_1, 'laser_2, 'laser_3, 'laser_4, 'yellow, 'stripes_h, 'stripes_v, 'stripes_b, 'stripes_refinery)

  def getMap = map.toMap

  def registerTexture(event: TextureStitchEvent.Pre): Unit = {
    val textureMap = event.getMap
    LaserType.values().foreach(laserType => textureMap.registerSprite(null, laserType.location()))
    symbols.foreach(s => textureMap.registerSprite(null, new ResourceLocation(QuarryPlus.modID, "entities/" + s.name)))
  }

  def putTexture(event: TextureStitchEvent.Post): Unit = {
    val textureMap = event.getMap
    LaserType.values().foreach(laserType => map.put(laserType.symbol, textureMap.getSprite(laserType.location())))
    symbols.foreach(s => map.put(s, textureMap.getSprite(new ResourceLocation(QuarryPlus.modID, "entities/" + s.name))))
  }
}
