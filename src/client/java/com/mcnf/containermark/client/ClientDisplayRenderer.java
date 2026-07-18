package com.mcnf.containermark.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.world.ClientWorld;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Manages client-side floating text displays for standalone mode.
 * Creates TextDisplay entities on the client only (not synced to server).
 */
public class ClientDisplayRenderer {

    private static final List<ClientDisplay> activeDisplays = new ArrayList<>();
    private static final int MAX_DISPLAYS = 10;

    private static class ClientDisplay {
        final UUID uuid;
        final long removalTick;
        final BlockPos pos;

        ClientDisplay(UUID uuid, long removalTick, BlockPos pos) {
            this.uuid = uuid;
            this.removalTick = removalTick;
            this.pos = pos;
        }
    }

    /**
     * Spawn a client-side floating text display.
     */
    public static void spawnDisplay(BlockPos pos, Text text, int durationSeconds) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.6;
        double z = pos.getZ() + 0.5;

        DisplayEntity.TextDisplayEntity display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, client.world);
        display.setPosition(x, y, z);
        display.setNoGravity(true);

        // Use reflection for private setters (1.21.11 API change)
        invokePrivate(DisplayEntity.TextDisplayEntity.class, display, "setText", Text.class, text);
        invokePrivate(DisplayEntity.class, display, "setBillboardMode", DisplayEntity.BillboardMode.class, DisplayEntity.BillboardMode.CENTER);
        invokePrivate(DisplayEntity.TextDisplayEntity.class, display, "setBackground", int.class, 0x60000000);
        invokePrivate(DisplayEntity.TextDisplayEntity.class, display, "setDisplayFlags", byte.class, DisplayEntity.TextDisplayEntity.SHADOW_FLAG);
        invokePrivate(DisplayEntity.class, display, "setViewRange", float.class, 2.0f);

        // Add to client world (no server sync)
        ((ClientWorld) client.world).addEntity(display);

        long removalTick = client.world.getTime() + (long) durationSeconds * 20L;
        UUID uuid = display.getUuid();

        // Enforce limit
        while (activeDisplays.size() >= MAX_DISPLAYS) {
            ClientDisplay oldest = activeDisplays.remove(0);
            removeDisplay(oldest.uuid);
        }

        activeDisplays.add(new ClientDisplay(uuid, removalTick, pos));
    }

    /**
     * Tick: remove expired displays.
     */
    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        long currentTick = client.world.getTime();
        Iterator<ClientDisplay> it = activeDisplays.iterator();
        while (it.hasNext()) {
            ClientDisplay d = it.next();
            if (currentTick >= d.removalTick) {
                removeDisplay(d.uuid);
                it.remove();
            }
        }
    }

    /**
     * Clear all displays (on disconnect).
     */
    public static void clear() {
        for (ClientDisplay d : activeDisplays) {
            removeDisplay(d.uuid);
        }
        activeDisplays.clear();
    }

    private static void removeDisplay(UUID uuid) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        net.minecraft.entity.Entity entity = client.world.getEntity(uuid);
        if (entity != null) {
            entity.discard();
        }
    }

    private static void invokePrivate(Class<?> clazz, Object obj, String methodName, Class<?> paramType, Object value) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramType);
            method.setAccessible(true);
            method.invoke(obj, value);
        } catch (Exception ignored) {}
    }
}
