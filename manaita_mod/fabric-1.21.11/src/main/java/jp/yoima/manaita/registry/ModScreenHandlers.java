package jp.yoima.manaita.registry;

import jp.yoima.manaita.ManaitaMod;
import jp.yoima.manaita.screen.ManaitaScreenHandler;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public final class ModScreenHandlers {
    public static final ScreenHandlerType<ManaitaScreenHandler> MANAITA = Registry.register(
            Registries.SCREEN_HANDLER,
            ManaitaMod.id("manaita"),
            new ScreenHandlerType<>(ManaitaScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
    );

    private ModScreenHandlers() {
    }

    public static void register() {
        // no-op: static initialization performs registration
    }
}

