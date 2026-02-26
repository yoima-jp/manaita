package jp.yoima.manaita.registry;

import java.util.EnumMap;
import java.util.Map;
import jp.yoima.manaita.ManaitaMod;
import jp.yoima.manaita.block.ManaitaBoardBlock;
import jp.yoima.manaita.block.ManaitaTier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    private static final Map<ManaitaTier, Item> BOARD_ITEMS = new EnumMap<>(ManaitaTier.class);

    public static final ManaitaBoardBlock WOODEN_MANAITA = registerBoard(ManaitaTier.WOODEN, AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(0.5f).nonOpaque().sounds(BlockSoundGroup.WOOD));
    public static final ManaitaBoardBlock STONE_MANAITA = registerBoard(ManaitaTier.STONE, AbstractBlock.Settings.copy(Blocks.COBBLESTONE).strength(1.5f).nonOpaque().sounds(BlockSoundGroup.STONE));
    public static final ManaitaBoardBlock IRON_MANAITA = registerBoard(ManaitaTier.IRON, AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(2.5f).nonOpaque().sounds(BlockSoundGroup.METAL));
    public static final ManaitaBoardBlock GOLD_MANAITA = registerBoard(ManaitaTier.GOLD, AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).strength(2.5f).nonOpaque().sounds(BlockSoundGroup.METAL));
    public static final ManaitaBoardBlock DIAMOND_MANAITA = registerBoard(ManaitaTier.DIAMOND, AbstractBlock.Settings.copy(Blocks.DIAMOND_BLOCK).strength(3.0f).nonOpaque().sounds(BlockSoundGroup.METAL));

    private ModBlocks() {
    }

    private static ManaitaBoardBlock registerBoard(ManaitaTier tier, AbstractBlock.Settings settings) {
        String id = tier.getId() + "_manaita";
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, ManaitaMod.id(id));
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, ManaitaMod.id(id));

        ManaitaBoardBlock block = Registry.register(Registries.BLOCK, blockKey, new ManaitaBoardBlock(tier, settings.registryKey(blockKey)));
        Item item = Registry.register(Registries.ITEM, itemKey, new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey()));
        BOARD_ITEMS.put(tier, item);
        return block;
    }

    public static Item itemFor(ManaitaTier tier) {
        return BOARD_ITEMS.get(tier);
    }

    public static void register() {
        // no-op: static initialization performs registration
    }
}
