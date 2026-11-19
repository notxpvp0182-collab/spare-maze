package com.ahmed.sparemaze.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpareMaze-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sparemaze.json");

    private static SpareMazeConfig config = new SpareMazeConfig();

    private ConfigManager() {}

    public static SpareMazeConfig getConfig() {
        return config;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            config = new SpareMazeConfig();
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            SpareMazeConfig loaded = GSON.fromJson(reader, SpareMazeConfig.class);
            if (loaded != null) {
                config = loaded;
                sanitizeConfig();
            } else {
                config = new SpareMazeConfig();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load Spare Maze config, restoring defaults", e);
            config = new SpareMazeConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save Spare Maze config", e);
        }
    }

    public static void restoreDefaults() {
        config = new SpareMazeConfig();
        save();
    }

    private static void sanitizeConfig() {
        if (config.pearl == null) config.pearl = new PearlConfig();
        if (config.mace == null) config.mace = new MaceConfig();
        if (config.axe == null) config.axe = new AxeConfig();
        if (config.spear == null) config.spear = new SpearConfig();
    }
}
