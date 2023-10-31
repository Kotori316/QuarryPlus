## Version LATEST for 1.19.2

* Add Faster Work Module, which can fasten quarry work x2 or x3
  * Be careful, this module can cause heavy lags
  * Disabled by default, change `repeat_tick_module` to `true` to use this module
* Limit the number of chaining of Dummy Block. #278
* Improve logging of machine. #275

## Version for 1.19.2

* Fix a bug of save/load of Chunk Destroyer
* Add server config to disable machine in specific dimension
* Fix deadlock with some item transfer mod
* The range of Flexible Marker is now configurable
* Show progress in QuarryPlus and ChunkDestroyer in Status Checker log
* May fix item duplication in Workbench
* Fixed a bug where swapping items in Workbench Plus could give over-stacked items. #253
* Changed versioning.
  * Include MC version into QuarryPlus version.
* Added IC2 Classic integration.
  * You can use EU to supply energy.
  * The conversion rate can be changed in config. The default is 1 EU = 4 FE.

## Version 18.17

* Fixed a bug where swapping items in Workbench Plus could give over-stacked items. #253

## Version 19.10.9

* Improved GUI of Chunk Destroyer.
  * Added checkbox to change setting.
  * The default setting is loaded from client config even in server world.
  * The checkbox doesn't have correct bounding box, but I'll fix later.
* Added Chunk-by-Chunk mode to Chunk Destroyer
  * In this setting, the machine works for a chunk first, then goes to next chunk.
  * It may reduce server load.

## Version 19.10.8

* Fixed a bug where mining well didn't finish when it tried to remove fluid. #247
* Fixed a bug where setting of mining well(digMinY) was reverted after it started.
* Internal changes for Chunk Destroyer.

## Version 19.10.7

* Fixed a bug where mining well didn't work under `y=0`. #243
* Make mining well enchantable. #242
  * Efficiency to work faster

## Version 19.10.6

* Added Void Module, which removes items mined by Quarry.
  * See [wiki](https://github.com/Kotori316/QuarryPlus/wiki/VoidModule) for description.

## Version 19.10.5

* Fixed a bug where Bedrock Module can't be crafted in some situation. (#235)
* Fixed typo of Exp Module in English. (#232)

## Version 19.10.4

* Log item transfer.
* Log if block has no drops.

## Version 19.10.3

* Log item conversion.
* Removed unused files.

## Version 19.10.2

* Add config option to disable chunk loading of quarry.
  * You can use chunk loaders from other mods without conflicts.
* Add a logger to trace almost all work of quarry.

## Version 19.10.1

* Not to check protection chunks too much.
* Fixed a bug that QuarryPlus removes blocks in protected chunks when it tries to place frame.

## Version 19.10

* Add config option to extract items in Workbench Plus.

## Version 19.9

* Add REI plugin.

## Version 19.8

* Added config option to limit the width of area where Chunk Destroyer works.
  * `chunkDestroyerLimit` in config.

## Version 19.7

* Improved renderers of Waterlogged Markers.
* Fixed bugs of Enchantment Mover in dedicated server. [#212](https://github.com/Kotori316/QuarryPlus/issues/212)

## Version 19.6

* Limit enchantment of machines by config. #209
* Enchantments whose level is over default limitation can be used now.

## Version 19.5

* Update to 1.19.2

## Version 19.4

* Update to 1.19.1

## Version 19.3

* Update forge

## Version 19.2

* Update forge
* Fixed - SFQ didn't mine Obsidian. Close #201

## Version 19.1

* Update Forge
* Update code for 1.19 JEI. Close #198
* Added Tags for Markers.

## Version 19.0

* Update for 1.19
* **Sorry, but this version is not manually tested. If you find bugs, please report.**
* Removed recipe for Marker Plus.

## Version 18.12

* Changed the content of Flexible Marker GUI.
  * Relative positions are added.
* Quarry now stops working in protected area by FTB Chunks.

## Version 18.11

* Fixed - the drop item of Waterlogged Marker was wrong.
* Fixed - Waterlogged Marker didn't show line when RS-powered.
* Simplified the shape of frame.
* Changed rendering of guide line of marker.
* Add config to hide far guide-line
* Remove frames after quarry is removed.
* Changed range of Flexible Marker

## Version 18.10

* Add Waterlogged Markers
* Changed placer behavior, to replace replaceable blocks.

## Version 18.9

* Fixed wrong parameter name in EnchantmentIngredient in json decoding.
* Fixed a bug that enchantment mover could move enchantment from iron pickaxe. Now it can move enchantments from armors.
* Added Remote Placer. [#179](https://github.com/Kotori316/QuarryPlus/issues/179)
* Create ko_kr.json.
* Update to recommend version of Forge.

## Version 18.8

* Update for 1.18.2

## Version 18.7

* Fixed - Filler didn't save the inventory.

## Version 18.6

* Fixed - Flexible Marker can't be used under 0.
* Changed - the value change in Y Setter GUI is now the same as Chunk Marker. (Shift: 16, Ctrl: 4, Other: 1)
* Added - Filler
* Added - Filler Module
  * You can use it with QuarryPlus and Chunk Destroyer, then Stone will be placed in the work area.

## Version 18.5

* Fixed - the calculation of Quarry Energy was wrong. Close [#166](https://github.com/Kotori316/QuarryPlus/issues/166)

## Version 18.4

* Added some config option to change power setting.
* Chunk Destroyer now removes some base blocks (no drop).

## Version 18.3

* Fixed a critical bug in QuarryPlus. The nbt data was not saved correctly in some situation.
* Added Solid Fuel Quarry.

## Version 18.2

* Release for 1.18.1

## Version 18.1

* Added JEI integration
* Fixed advancement of recipe had wrong format.
* Fixed potential issue of sync.

## Version 18.0

* Initial release for 1.18

## Version 17.7

* Fixed waterlogged blocks were not removed if QuarryPlus doesn't have Pump Module.
* Sort items in Creative Tab.
* Log debug info to `debug.log`.
* Improved chunk loading.
* "All" condition is added to MiniQuarry.
  * Useful to enable allow-only mode.
  * The mode MiniQuarry removes blocks only in the list.

## Version 17.6-SNAPSHOT

* Fixed crash in dedicated server. [#141](https://github.com/Kotori316/QuarryPlus/issues/141)
* Show disable message of machines as game info.
* Added Mini Quarry. [#137](https://github.com/Kotori316/QuarryPlus/pull/137)

## Version 17.5-SNAPSHOT

* Set automatic module name
* Fixed crash when player opened WorkbenchPlus. [#139](https://github.com/Kotori316/QuarryPlus/issues/139)
* Sort machine names in config.
* Slow down quarry speed when it's breaking blocks inside frame.
* Added Spawner Controller.

## Version 17.4-SNAPSHOT

* Fixed crash of Chunk Destroyer GUI.
  * Open Module inventory directly if player has module item in hand.
* Implement item/fluid handler for machines.
  * This may improve connection of pipes from other mods.

## Version 17.3-SNAPSHOT

* Added Chunk Destroyer
  * It is a kind of quarry, but it removes to y=0 at once.
  * Requires over 1000 FE/t.
    * Requires about 80000 FE/t if it removes Bedrock.
  * Removes fluids.
  * Modules can be installed.
* Fixed some GUIs remained opened after player left.
* Fixed quick move in inventory didn't work in GUI of Creative Generator.
* Warn if machine requires energy that is over its capacity.
* Changed energy format for debug log.
* Mover now gives back items when GUI is closed instead of dropping.

## Version 17.2-SNAPSHOT

* Added Replacer
  * In default, this is disabled. Change entry in config to enable this machine.
  * Both block and module item are available.
  * Block: place the replacement block on the top od replacer.
  * Module Item: right click to the replacement block.
  * Quarry and Advanced Pump will replace blocks instead of remove.
* Added Book Enchantment Mover
  * In default, this is disabled. Change entry in config to enable this machine.
  * This machine can move enchantments on Enchanted Book to machines like quarry.

## Version 17.1-SNAPSHOT

* Added Exp Module(item)
  * This module can collect experiment points of ores quarry dug.
* Added Exp Pump(block)
  * Same as item.
* Fixed a bug of saving tile data
* Improvement of Workbench
  * The update timing of recipes was improved.

## Version 17.0-SNAPSHOT

* The first release version for Minecraft 1.17.1
  * **Features**
    * QuarryPlus
    * Marker Plus
      * Also, 2 other markers
    * Workbench Plus, to craft items in this mod.
    * Modules
      * Pump Module
      * Bedrock Remove Module(disabled in default)
      * Energy Module
    * Advanced Pump
    * Enchantment Mover
