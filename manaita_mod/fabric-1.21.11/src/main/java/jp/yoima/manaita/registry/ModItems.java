package jp.yoima.manaita.registry;

import jp.yoima.manaita.ManaitaMod;
import jp.yoima.manaita.item.PlateItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModItems {
    public static final Item PLATE = register("plate", PlateItem::new);

    private ModItems() {
    }

    private static Item register(String id, java.util.function.Function<Item.Settings, Item> factory) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, ManaitaMod.id(id));
        Item item = factory.apply(new Item.Settings().registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void register() {
        // no-op: static initialization performs registration
    }
}
