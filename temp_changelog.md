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