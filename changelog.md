## 1.19

### Changes

* Removed - Marker Plus. Use Flexible Marker instead.
* Added - Plugin for Jade. WTHIT is already supported.
* Added - A tag of markers.
* Added - GUI of QuarryPlus to change whether to remove fluids.
* Added - Version checker integration

### Fixed

* Fixed - Recipes of some machines were wrong. Close #194
* Fixed - Fluid Unit was wrong.

### Misc

* Update for 1.19

## 1.18

### Addition

* Added option to remove frames after Quarry is removed.
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

* Fixed - performance issue of frame block and marker.
* Fixed - Pickaxe mineable blocks didn't drop anything. Close #184
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

* Improved - Use fake player to place blocks.
* Improved - The screen pf Flexible Marker now shows relative position.
* Changed - Flexible Marker can now select up to 256 blocks.
* Use conventional tags in recipe.
* Update for 1.18.2
* Changed - The amount of Y if Shift or Ctrl is pressed.
* Changed - Reduced work energy of Chunk Destroyer. (Changed coefficient of Efficiency)
* Changed the order to search inventories. Some tests for MachineStorage.
* Update for 1.18
