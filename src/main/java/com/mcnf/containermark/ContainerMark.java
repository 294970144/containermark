package com.mcnf.containermark;

import com.mcnf.containermark.config.MarkConfig;
import com.mcnf.containermark.mark.DisplayManager;
import com.mcnf.containermark.mark.MarkHandler;
import com.mcnf.containermark.network.ModNetworking;
import com.mcnf.containermark.notify.PlayerSelector;
import com.mcnf.containermark.team.TeamCommand;
import com.mcnf.containermark.team.TeamManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerMark implements ModInitializer {
    public static final String MOD_ID = "containermark";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[ContainerMark] Initializing...");

        // Load config
        MarkConfig.load();

        // Register network payloads and receivers
        ModNetworking.register();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TeamCommand.register(dispatcher);
        });

        // Register server tick for display management
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DisplayManager.tick(server);
        });

        LOGGER.info("[ContainerMark] Initialized successfully.");
    }
}
