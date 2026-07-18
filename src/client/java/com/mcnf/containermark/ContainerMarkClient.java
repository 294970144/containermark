package com.mcnf.containermark;

import com.mcnf.containermark.client.ClientDisplayRenderer;
import com.mcnf.containermark.client.ClientMarkHandler;
import com.mcnf.containermark.client.ClientModState;
import com.mcnf.containermark.client.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ContainerMarkClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ContainerMark.LOGGER.info("[ContainerMark] Client initializing...");

        // Register keybindings
        KeyBindings.register();

        // Register client tick handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientMarkHandler.onTick(client);
        });

        // Detect server mod on join, reset on disconnect
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(() -> {
                ClientModState.detectServer();
                ContainerMark.LOGGER.info("[ContainerMark] Server mod detected: {}", ClientModState.isServerInstalled());
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientModState.reset();
            ClientDisplayRenderer.clear();
        });

        ContainerMark.LOGGER.info("[ContainerMark] Client initialized.");
    }
}
