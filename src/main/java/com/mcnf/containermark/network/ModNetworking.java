package com.mcnf.containermark.network;

import com.mcnf.containermark.mark.MarkHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModNetworking {
    public static void register() {
        // Register C2S payload codecs
        PayloadTypeRegistry.playC2S().register(MarkWorldPayload.ID, MarkWorldPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MarkSlotPayload.ID, MarkSlotPayload.CODEC);

        // Register server-side receivers
        ServerPlayNetworking.registerGlobalReceiver(MarkWorldPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> MarkHandler.handleWorldMark(player, payload));
        });

        ServerPlayNetworking.registerGlobalReceiver(MarkSlotPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> MarkHandler.handleSlotMark(player, payload));
        });
    }
}
