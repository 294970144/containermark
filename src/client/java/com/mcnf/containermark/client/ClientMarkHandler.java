package com.mcnf.containermark.client;

import com.mcnf.containermark.network.MarkWorldPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClientMarkHandler {

    public static void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // Only handle world marking when no screen is open (not in container UI)
        if (client.currentScreen != null) return;

        while (KeyBindings.markKey.wasPressed()) {
            // Must be sneaking to mark world targets
            if (!client.player.isSneaking()) continue;

            HitResult hit = client.crosshairTarget;
            if (hit == null) continue;

            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                // Check if block has inventory (container)
                if (isContainer(client.world, pos)) {
                    if (ClientModState.isServerInstalled()) {
                        // Server mode: send network packet
                        var dim = client.player.getEntityWorld().getRegistryKey().getValue();
                        ClientPlayNetworking.send(new MarkWorldPayload(
                            MarkWorldPayload.TYPE_BLOCK, pos, -1, dim
                        ));
                    } else {
                        // Standalone mode: client-side processing
                        ClientMarkProcessor.handleWorldMark(client);
                    }
                }
            } else if (hit.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) hit).getEntity();
                if (entity instanceof ItemEntity) {
                    BlockPos pos = entity.getBlockPos();
                    if (ClientModState.isServerInstalled()) {
                        // Server mode: send network packet
                        var dim = client.player.getEntityWorld().getRegistryKey().getValue();
                        ClientPlayNetworking.send(new MarkWorldPayload(
                            MarkWorldPayload.TYPE_ITEM_ENTITY, pos, entity.getId(), dim
                        ));
                    } else {
                        // Standalone mode: client-side processing
                        ClientMarkProcessor.handleWorldMark(client);
                    }
                }
            }
        }

        // Tick client displays in standalone mode
        if (!ClientModState.isServerInstalled()) {
            ClientDisplayRenderer.tick();
        }
    }

    private static boolean isContainer(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof net.minecraft.inventory.Inventory) return true;
        return world.getBlockState(pos).createScreenHandlerFactory(world, pos) != null;
    }
}
