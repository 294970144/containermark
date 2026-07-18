package com.mcnf.containermark.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcnf.containermark.ContainerMark;
import com.mcnf.containermark.notify.NotifyRange;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("containermark.json");

    private static MarkConfig instance = new MarkConfig();

    public NotifyRange notifyRange = NotifyRange.ALL;
    public double radius = 64.0;
    public int displayDuration = 30;
    public boolean particleEnabled = true;
    public int particleDurationSeconds = 5;
    public int markCooldownSeconds = 2;
    public int maxDisplaysPerPlayer = 5;

    public static MarkConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                instance = GSON.fromJson(json, MarkConfig.class);
                if (instance == null) instance = new MarkConfig();
                ContainerMark.LOGGER.info("[ContainerMark] Config loaded from {}", CONFIG_PATH);
            } catch (Exception e) {
                ContainerMark.LOGGER.error("[ContainerMark] Failed to load config, using defaults", e);
                instance = new MarkConfig();
            }
        } else {
            ContainerMark.LOGGER.info("[ContainerMark] No config file found, creating default at {}", CONFIG_PATH);
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(instance));
        } catch (IOException e) {
            ContainerMark.LOGGER.error("[ContainerMark] Failed to save config", e);
        }
    }
}
