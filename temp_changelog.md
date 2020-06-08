**This mod requires a library, [Scalable Cat's Force](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force).**
## Version 15.3.1
* Fixed a crash in launching server by LoadingFailedException. [#91](https://github.com/Kotori316/QuarryPlus/issues/91)

## Version 15.3.0
* Now NewQuarry can accept setting of ListEditor and Template.
  * You can select which blocks quarry digs with enchantments.
* Changed serialize process of blacklist.
* Added new machine, Mini Quarry.
  * A simple machine which allows you to automatically break blocks.
  * Can be used in tree farm.
  * To use machine,
    * Place
    * Open GUI and put tools required to break block. (Pickaxe for stones, Axe for wood, shovel for dirt)
    * Send RS signal(Pulse). Toggles working and waiting with a redstone pulse.

## Version 15.2.9
* **Updated language loader**. Please update library mod to newer than 2.13.2. You can download from above link.
* Internal changes.
* Make "Fuel Module" stackable, then you can insert the module and get maximum 2560 FE/t with no resources.
* Fixed a bug where Bedrock Module didn't work with low energy supplier.

## Version 15.2.8
* Fixed a bug where the text in StatusChecker was wrong (In a multi server).

## Version 15.2.7
* Fixed the crash of `java.lang.NoSuchMethodError: net.minecraft.item.Item.func_200296_o()Lnet/minecraft/util/text/ITextComponent;`

## Version 15.2.6
* Fixed the replacer didn't work for bedrock.
* added new example of blacklist
* Fixed miningwell plus digs block before energy supplied.
* Forge version up to 31.1.27
* BlackList of Machines.
* Fixed quarry area faced wrong direction.
* Fixed area finding method seemed wrong.

## Version 15.2.5
* Fixed Solid Fuel Quarry stops when working.
* Refactoring.

## Version 15.2.4
* Now, pipe from Mekanism looks connected to Quarries.
* Quarries now return dummy handlers for ITEM and FLUID capability.

## Version 15.2.3
* Implemented "Placer Plus".
* Implemented data generators.
* Internal changes
  * Changed texture name.
  * Removed unused code.

## Version 15.2.2
* Update forge and mcp mapping.
* Fixed Torch Module didn't work.

## Version 15.2.1
* Allow minus energy when digging. #78.
* Implemented area limitation for ChunkDestroyer.
* Fixed quarry drill was rendered in wrong dimension.
* Imported changes from 1.14.4 2020/02/14.
    - Many internal changes.
    - Chain breaking of frame expanded
    - More info on Status Checker
    - Faster pump
    - Fuel Module for AdvPump

## Version 15.2.0
* Update to Minecraft 1.15.2

## Version 15.0.3
* Fixed solid fuel quarry had no renderer.
* Changed default value of `NoEnergy` false.
* Fixed crashes and infinite loops of Chunk Destroyer.
* Render improvement. Not to render far lines.

## Version 15.0.2
* Add support of JEI.

## Version 15.0.1-SNAPSHOT
* Added the renderer for QuarryPlus.
* Disabled own font renderer in WorkbenchPlus.
* Added recipe hint of WorkbenchPlus.

## Version 15.0.0-SNAPSHOT
* First version for Minecraft 1.15.1
* Renderer for QuarryPlus and Old QuarryPlus aren't implemented because of some rendering issue.
