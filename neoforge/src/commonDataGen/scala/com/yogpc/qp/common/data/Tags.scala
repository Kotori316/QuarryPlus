package com.yogpc.qp.common.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.resources.Identifier
import net.minecraft.tags.{BlockTags, ItemTags}

val markerBlockTag = BlockTags.create(Identifier.fromNamespaceAndPath(QuarryPlus.modID, "markers"))
val markerItemTag = ItemTags.create(Identifier.fromNamespaceAndPath(QuarryPlus.modID, "markers"))
val quarryPickaxeTag = ItemTags.create(Identifier.fromNamespaceAndPath(QuarryPlus.modID, "quarry_pickaxes"))
