# Version 12.2.4
- Added -  module system for Chunk Destroyer. (#53)
- Improvement - You can get the y-level of machine in tile's inventory GUI.

# Version 12.2.3
* Added torch module and fuel module. [#61](https://github.com/Kotori316/QuarryPlus/issues/61)
* Gather drops before finishing quarry work.
* Added a predicate that prevent from inserting same kind of module.
* Unique return type
* Added tick event
* Changed field name
* Inline Symbol instance
* Send localized name when quarry restarted
* Update lang file
* Support chain breaking of MCAssistant.
* Use BC's marker to make dig area

# Version 12.2.2
* Fixed - NPE when pump module is loaded.
* Improvement - reduced energy to remove fluids.
* Fixed #60
* New quarry connects to fluid pipe.
* Fixed - fluid in new quarry is lost when loading
* Fixed - fluid is replacer to dummy block
* Remove nbt tag when xp is extracted.

# Version 12.2.1
* Fixed - wrong reading config option

# Version 12.2.0
* Changed version to {mcVersion}.{major}.{minor}
* Added new quarry, which has module system to enhance its function.

# Version 1.1.0
* Hide enchantment shown in tooltip of List Template.
* Fixed bug of saving nbt of Replacer.
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
* Sneak and right click with AE's wrench and other mods's one to remove machine.
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
* Made quarry head moving much faster. Change FastQuarryHeadMove in config to true.

# Version 1.0.0
* Now you can modify recipes of WorkbenchPlus. Copy file from `config/quarryplus/defaultrecipes` to `config/quarryplus/recipes` and change `UseHardCodeRecipe` in config to false.
* Made Frame mode work faster.

# Version 0.11.2
* Fixed a crash when you shift-clicked item in WorkbenchPlus.
* Added a condition quarryplus:machine_enabled, which checks if a machine which has the name in "value" field is enabled.
* Fixed an issue where you can't craft items with WorkbenchPlus due to recreating tile every ticks.

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
