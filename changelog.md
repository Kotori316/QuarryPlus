## 1.18

### Addition

* Added - Waterlogged Markers
* Added - Remote Placer
* Added - Filler
* Added - config option to disable removing common material in ChunkDestroyer.
* Added recipe of Status Checker.
* Added support of lib block attributes in 1.18.
* Added integration of Tech Energy. Close #154
* Added config entry for Creative Generator.
* Support WTHIT, an alternative of WAILA.

### Fixed

* Fixed - Filler didn't accept items from Hopper. Close #175
* Fixed - AdvQuarry ignores nether portal.
* Fixed - Flexible Marker can't be used under 0.
* Fixed - performance issue of rendering of ChunkDestroyer.
* Fixed - Frame line of QuarryPlus disappeared after world was loaded.
* Fixed a crash related to client sync.
* Fixed - Enchantment couldn't be moved from Enchanted Book to machines with Anvil.
* Fixed - Chunk Destroyer couldn't be enchanted in Enchantment Table.
* Fixed - Quarry configuration was sometimes deleted. Close #157
* Fixed - Guideline(yellow and black) wasn't shown after placing quarry.
* Removed internal API call in config. Close #151

### Misc

* Use conventional tags in recipe.
* Update for 1.18.2
* Changed - The amount of Y if Shift or Ctrl is pressed.
* Changed - Reduced work energy of Chunk Destroyer. (Changed coefficient of Efficiency)
* Changed the order to search inventories. Some tests for MachineStorage.
* Update for 1.18
