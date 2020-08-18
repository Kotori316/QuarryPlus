# Version 12.4.5
* Catch errors of loading black list.

# Version 12.4.4
* Improved filler work.
  * It can place colored wools.
  * Solar panels from IC2 and EnderIO, too

# Version 12.4.3
* Removed unnecessary method of WorkbenchPlus

# Version 12.4.1
* Template improved
  * Fortune Template setting will be applied to quarry correctly.

# Version 12.4.0
* You can use ListEditor and Template for New QuarryPlus.
  * It's useful when quarry has both Fortune and Silktouch.
  * Changing `DisableEnchantMoverFromBook` false allows you to move both enchantments at the same time.

# Version 12.3.9
* Fixed the quarry fired a harvesting event every tick.
* Use own pickaxe to break blocks. (Harvesting Level = 5 (2 level upper than diamond), Tier = DIAMOND)

# Version 12.3.8
* Fixed that quarry stopped when building frames.
* Fixed the old quarry required energy before replacing fluids.
* Added a recipe to convert from Old Quarry Plus to Quarry Plus.

# Version 12.3.7
* Improved quarry work.
* Reduced energy to remove fluids. (Unbreaking of machine will be applied.)
* Fixed the quarry was too faster. #85
* Log energy consumption detail.

# Version 12.3.6
- Changed - Machines now accept "Insane Voltage"(up to 8192 EU/t).

# Version 12.3.5
- Fixed - Quarry freezes when mining bedrock ore if you use low energy supplier(less than 1000 RF/t).

# Version 12.3.4
- Removed log message of QuarryStorage.
- QuarryPlus keeps order of items to output to chest.
- QuarryPlus now push fluid every 1 second.
- Added black list for mining machines.
- Support ores added by [Bedrock Ores](https://www.curseforge.com/minecraft/mc-mods/bedrockores).

# Version 12.3.3
- Softer attachment blocks. (Pump, ExpPump, Replacer)
- Make tile blocks removable when IC2 new wrench is used.
- Update dependencies. (IC2 and BC)

# Version 12.3.2
- Fixed issue of energy setting.

# Version 12.3.1
- Added `delete` button to adv pump.
- Improved chain breaking of quarry frame
- Added a filler. (disabled in default)

# Version 12.3.0
- Working on a crash when you remove working pump.

# Version 12.2.9
- Working on an issue where quarry stops when removing sea.

# Version 12.2.8
- Added - Status Checker GUI for QuarryPlus. Enchantments, requiring energy and internal storage info will be shown. 
[#69](https://github.com/Kotori316/QuarryPlus/issues/69)

# Version 12.2.7
- Trying to fix a crash of `IllegalArgumentException` in using new quarry. [#68](https://github.com/Kotori316/QuarryPlus/issues/68)

# Version 12.2.6
- Fixed a render issue with other mods.
- Added git hush to jar file.

# Version 12.2.5
- Fixed - Solid Fuel Quarry consumed lava bucket.
- Improvement - Workbench act as receiving energy for 10 seconds when `NoEnergy` is `true`.
- Improvement - Solid Fuel Quarry drops fuel item when removed.
- Improvement - Solid Fuel Quarry shows fuel count on its GUI.
- Improvement - gather items from wider area.
- Improvement - TorchModule place torch only once a tick.

# Version 12.2.4
- Added - module system for Chunk Destroyer. [#53](https://github.com/Kotori316/QuarryPlus/issues/53)
- Improvement - You can get the y-level of machine in a tile's inventory GUI.
- Fixed - TorchModule placed torch at invalid position.
- Fixed - The state of ExpPump wasn't changed when xps are collected.
- Fixed - Now all machines respect setting option `Disabled`.

# Version 12.2.3
* Added torch module and fuel module. [#61](https://github.com/Kotori316/QuarryPlus/issues/61)
* Gather drops before finishing quarry work.
* Added a predicate that prevent from inserting same kind of module.
* Unique return type
* Added tick event
* Changed field name
* Inline Symbol instance
* Send the localized name when quarry restarted
* Update lang file
* Support chain breaking of MCAssistant.
* Use BC's marker to make dig area

# Version 12.2.2
* Fixed - NPE when pump module is loaded.
* Improvement - reduced energy to remove fluids.
* Fixed #60
* The new quarry connects to fluid pipe.
* Fixed - fluid in the new quarry is lost when loading
* Fixed - fluid is replacer to dummy block
* Remove nbt tag when xp is extracted.

# Version 12.2.1
* Fixed - wrong reading config option

# Version 12.2.0
* Changed the version to {mcVersion}.{major}.{minor}
* Added new quarry, which has module system to enhance its function.

# Version 1.1.3
* The position of Quarry's dummy player is set to the point quarry is removing.
* Added support of chain break of [MCAssistant](https://www.curseforge.com/minecraft/mc-mods/mcassistant).

# Version 1.1.2
* Reverted [272a28bc](https://github.com/Kotori316/QuarryPlus/commit/272a28bcf97149a24f79f3411a1b6f866b5db518) to avoid crash with the old forge.

# Version 1.1.1
* Fixed a bug where quarry is reset when work finished if BC's pipe is connected.

# Version 1.1.0
* Hide enchantment shown in the tooltip of List Template.
* Fixed a bug of saving nbt of Replacer.
* Added zh_cn.lang

# Version 1.0.9
* Added List Template. Right click any block to open GUI, place block on right slot and click "Add". Then, right click a machine with List Template to move setting.

# Version 1.0.8
* Now you can't move enchantments to machines which is incompatible with the enchantment. (e.g. You can't move Silktouch to machine with Smashing or Smelting from CoFHCore, Auto-Smelt from Ender Core.)

# Version 1.0.7
* PumpPlus can be enchanted with both Fortune and Silktouch is BookEnchMover is enabled.
* Update for New Wrench of IC2.
* Use ObfuscationReflectionHelper as refraction helper.

# Version 1.0.6
* Internal changes. Use long instead of double to store energy.
* Allowed QuarryPlus to be enchanted with Fortune AND Silktouch at the same time. [#51](https://github.com/Kotori316/QuarryPlus/issues/51)

# Version 1.0.5
* Sneak and right click with AE's wrench and other mods' one to remove machine.
* Reduced log spam.

# Version 1.0.4
* Add NoFrameMode to ChunkDestroyer. The mode skips building frame and start breaking blocks immediately.
* Add some tooltips to attachment machines. e.g. pumps
* Removed duplicated code.

# Version 1.0.3
* Add a new item, "Quarry Y Setter", which allows you to stop quarry work at a specific y level.
* Machines with ability of changing its texture when working have comparator output. (When working 15, otherwise 0)

# Version 1.0.2
* Support BuildCraft 7.99.21 and newer. Close [#47](https://github.com/Kotori316/QuarryPlus/issues/47).

# Version 1.0.1
* Working to fix a bug where QuarryPlus stops its work while removing flowing liquid with PumpPlus in frame mode.
If you have this problem, right click the PumpPlus with a stick or wrench to change PumpPlus's mode.
* Made quarry head moving much faster. Change FastQuarryHeadMove in the config to true.

# Version 1.0.0
* Now you can modify recipes of WorkbenchPlus. Copy file from `config/quarryplus/defaultrecipes` to `config/quarryplus/recipes` and change `UseHardCodeRecipe` in config to false.
* Made Frame mode work faster.

# Version 0.11.2
* Fixed a crash when you shift-clicked item in WorkbenchPlus.
* Added a condition quarryplus:machine_enabled, which checks if a machine which has the name in "value" field is enabled.
* Fixed an issue where you can't craft items with WorkbenchPlus due to recreating tile every tick.

# Version 0.11.1
* Now SolidQuarry doesn't push fuels to hopper. Close [#43](https://github.com/Kotori316/QuarryPlus/issues/43).

# Version 0.11.0
* Remove pipes when MiningwellPlus is removed. Close [#42](https://github.com/Kotori316/QuarryPlus/issues/42)
* Fixed java.lang.NullPointerException at EnergyDebug#getTime when tile is loaded.
* Config now outputs quarryplus_noDigBlocks.json.

# Version 0.10.9
* Fixed a problem where quarry didn't find a marker which implemented buildcraft.api.core.IAreaProvider.
* Changed logic to search markers.

# Version 0.10.8
* Internal changes

# Version 0.10.7
* Fixed a crash due to NullPointerException in FakePlayer. [#41](https://github.com/Kotori316/QuarryPlus/issues/41)
* Updated for new JEI newer than 4.12.0
