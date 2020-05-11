package com.yogpc.qp.render

import com.yogpc.qp.QuarryPlus
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent

import scala.collection.mutable

object Sprites {

  val instance: Sprites.type = this
  private[this] val map = mutable.Map.empty[Symbol, TextureAtlasSprite]
  private[this] final val symbols = List("laser_1", "laser_2", "laser_3", "laser_4", "yellow", "stripes_h", "stripes_v", "stripes_b", "stripes_refinery")
    .map(Symbol.apply)

  def getMap: Map[Symbol, TextureAtlasSprite] = map.toMap

  def registerTexture(event: TextureStitchEvent.Pre): Unit = {
    if (event.getMap.getBasePath == "textures") {
      LaserType.values().foreach(laserType => event.addSprite(laserType.location()))
      symbols.foreach(s => event.addSprite(new ResourceLocation(QuarryPlus.modID, "entities/" + s.name)))
    }
  }

  def putTexture(event: TextureStitchEvent.Post): Unit = {
    val textureMap = event.getMap
    if (textureMap.getBasePath == "textures") {
      LaserType.values().foreach(laserType => map.put(laserType.symbol, textureMap.getSprite(laserType.location())))
      symbols.foreach(s => map.put(s, textureMap.getSprite(new ResourceLocation(QuarryPlus.modID, "entities/" + s.name))))
    }
  }
}
