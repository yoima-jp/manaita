# Compatibility Notes

## Fabric 1.21.11

- Mappings: Yarn `1.21.11+build.4`
- Loom plugin: `fabric-loom` (`1.14-SNAPSHOT`)
- API changes handled:
  - `HorizontalFacingBlock` subclasses must implement `getCodec()`
  - `World#isClient` is now `world.isClient()`
  - `AbstractBlock#onStateReplaced` signature changed to server-world callback
  - `Item#appendTooltip` signature changed to consumer-based API
  - `BlockSoundGroup` package moved to `net.minecraft.sound`
- Registry key requirement:
  - `Item.Settings#registryKey(...)` must be set before item construction
  - `AbstractBlock.Settings#registryKey(...)` is set for custom blocks
