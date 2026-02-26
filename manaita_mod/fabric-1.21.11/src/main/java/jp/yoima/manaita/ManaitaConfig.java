package jp.yoima.manaita;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class ManaitaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final float DEFAULT_PLATE_DROP_CHANCE = 0.125f;
    private static final String FILE_NAME = "manaita.json";

    public float plateDropChance = DEFAULT_PLATE_DROP_CHANCE;
    public boolean enableCheat = false;

    private void sanitize() {
        if (Float.isNaN(this.plateDropChance)) {
            this.plateDropChance = DEFAULT_PLATE_DROP_CHANCE;
            return;
        }
        this.plateDropChance = Math.max(0.0f, Math.min(1.0f, this.plateDropChance));
    }

    public static ManaitaConfig load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        ManaitaConfig config = new ManaitaConfig();

        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                ManaitaConfig loaded = GSON.fromJson(reader, ManaitaConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (Exception e) {
                ManaitaMod.LOGGER.warn("Failed to read config {}, using defaults", configPath, e);
            }
        }

        config.sanitize();
        config.save(configPath);
        return config;
    }

    private void save(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            ManaitaMod.LOGGER.warn("Failed to write config {}", configPath, e);
        }
    }
}

