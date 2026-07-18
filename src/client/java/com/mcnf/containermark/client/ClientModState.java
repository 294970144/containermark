package com.mcnf.containermark.client;

import com.mcnf.containermark.network.MarkWorldPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Tracks whether the dedicated server has ContainerMark installed.
 * When false, the client falls back to standalone mode (local rendering + chat message).
 */
public class ClientModState {
    private static boolean serverInstalled = false;

    /**
     * Check on join if the server registered our C2S payloads.
     * Called from ClientPlayConnectionEvents.JOIN.
     */
    public static void detectServer() {
        serverInstalled = ClientPlayNetworking.canSend(MarkWorldPayload.ID);
    }

    /**
     * Reset on disconnect.
     */
    public static void reset() {
        serverInstalled = false;
    }

    public static boolean isServerInstalled() {
        return serverInstalled;
    }
}
