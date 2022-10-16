## Version 18.16

* Added config option to limit the width of area where Chunk Destroyer works.
  * `chunkDestroyerLimit` in config.

## Version 18.15

* Improved renderers of Waterlogged Markers.
* Fixed bugs of Enchantment Mover in dedicated server. [#212](https://github.com/Kotori316/QuarryPlus/issues/212)

## Version 18.14

* Limit enchantment of machines by config.
* Enchantments whose level is over default limitation can be used now.

## Version 18.13

* Improved SFQ work. Close #201

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