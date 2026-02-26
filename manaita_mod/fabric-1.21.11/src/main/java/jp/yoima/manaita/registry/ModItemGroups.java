package jp.yoima.manaita.registry;

import jp.yoima.manaita.ManaitaMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
    public static final ItemGroup MANAITA = Registry.register(
            Registries.ITEM_GROUP,
            ManaitaMod.id("main"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.manaita"))
                    .icon(() -> new ItemStack(ModItems.PLATE))
                    .entries((context, entries) -> {
                        entries.add(ModItems.PLATE);
                        entries.add(ModBlocks.WOODEN_MANAITA);
                        entries.add(ModBlocks.STONE_MANAITA);
                        entries.add(ModBlocks.IRON_MANAITA);
                        entries.add(ModBlocks.GOLD_MANAITA);
                        entries.add(ModBlocks.DIAMOND_MANAITA);
                    })
                    .build()
    );

    private ModItemGroups() {
    }

    public static void register() {
        // no-op: static initialization performs registration
    }
}
