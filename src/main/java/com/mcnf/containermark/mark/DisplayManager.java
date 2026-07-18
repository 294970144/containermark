package com.mcnf.containermark.mark;

import com.mcnf.containermark.config.MarkConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DisplayManager {

    private static final Map<UUID, ActiveDisplay> activeDisplays = new HashMap<>();
    private static final Map<UUID, List<UUID>> playerDisplays = new HashMap<>();

    private static class ActiveDisplay {
        final UUID entityUuid;
        final UUID ownerUuid;
        final long removalTick;
        final RegistryKey<World> worldKey;

        ActiveDisplay(UUID entityUuid, UUID ownerUuid, long removalTick, RegistryKey<World> worldKey) {
            this.entityUuid = entityUuid;
            this.ownerUuid = ownerUuid;
            this.removalTick = removalTick;
            this.worldKey = worldKey;
        }
    }

    public static void spawnDisplay(ServerWorld world, BlockPos pos, List<ItemStack> items, UUID ownerUuid, long currentTick) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.6;
        double z = pos.getZ() + 0.5;

        // Build display text
        Text displayText = buildDisplayText(items);

        // Create TextDisplay entity
        DisplayEntity.TextDisplayEntity display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
        display.setPosition(x, y, z);

        // Use reflection to call private setter methods (API changed in 1.21.11)
        invokePrivate(DisplayEntity.TextDisplayEntity.class, display, "setText", Text.class, displayText);
        invokePrivate(DisplayEntity.class, display, "setBillboardMode", DisplayEntity.BillboardMode.class, DisplayEntity.BillboardMode.CENTER);
        invokePrivate(DisplayEntity.TextDisplayEntity.class, display, "setBackground", int.class, 0x60000000);
        invokePrivate(DisplayEntity.TextDisplayEntity.class, display, "setDisplayFlags", byte.class, DisplayEntity.TextDisplayEntity.SHADOW_FLAG);
        invokePrivate(DisplayEntity.class, display, "setViewRange", float.class, 2.0f);

        // Spawn entity
        world.spawnEntity(display);

        // Track for cleanup
        long removalTick = currentTick + (long) MarkConfig.get().displayDuration * 20L;
        UUID entityUuid = display.getUuid();
        RegistryKey<World> worldKey = world.getRegistryKey();

        // Enforce per-player limit
        List<UUID> playerList = playerDisplays.computeIfAbsent(ownerUuid, k -> new ArrayList<>());
        while (playerList.size() >= MarkConfig.get().maxDisplaysPerPlayer) {
            UUID oldest = playerList.remove(0);
            ActiveDisplay old = activeDisplays.remove(oldest);
            if (old != null) {
                removeEntityFromWorld(world.getServer(), old);
            }
        }

        playerList.add(entityUuid);
        activeDisplays.put(entityUuid, new ActiveDisplay(entityUuid, ownerUuid, removalTick, worldKey));

        // Also schedule particles for this position
        ParticleScheduler.schedule(world, pos, currentTick);
    }

    public static void tick(MinecraftServer server) {
        // Tick particles
        ParticleScheduler.tick(server);

        if (activeDisplays.isEmpty()) return;

        long currentTick = server.getTicks();
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, ActiveDisplay> entry : activeDisplays.entrySet()) {
            if (currentTick >= entry.getValue().removalTick) {
                toRemove.add(entry.getKey());
            }
        }

        for (UUID uuid : toRemove) {
            ActiveDisplay display = activeDisplays.remove(uuid);
            if (display != null) {
                removeEntityFromWorld(server, display);
                // Clean up player tracking
                List<UUID> playerList = playerDisplays.get(display.ownerUuid);
                if (playerList != null) {
                    playerList.remove(uuid);
                    if (playerList.isEmpty()) {
                        playerDisplays.remove(display.ownerUuid);
                    }
                }
            }
        }
    }

    /**
     * Clean up all active displays for a player on disconnect.
     */
    public static void cleanupPlayer(UUID playerUuid) {
        List<UUID> playerList = playerDisplays.remove(playerUuid);
        if (playerList != null) {
            for (UUID entityUuid : playerList) {
                activeDisplays.remove(entityUuid);
            }
        }
    }

    private static void removeEntityFromWorld(MinecraftServer server, ActiveDisplay display) {
        ServerWorld world = server.getWorld(display.worldKey);
        if (world != null) {
            net.minecraft.entity.Entity entity = world.getEntity(display.entityUuid);
            if (entity != null) {
                entity.discard();
            }
        }
    }

    private static Text buildDisplayText(List<ItemStack> items) {
        if (items.isEmpty()) {
            return Text.literal("Empty").formatted(Formatting.GRAY);
        }

        var builder = Text.literal("");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) builder.append(Text.literal("\n"));
            ItemStack stack = items.get(i);
            builder.append(Text.literal(stack.getName().getString()).formatted(Formatting.WHITE));
            builder.append(Text.literal(" x" + stack.getCount()).formatted(Formatting.GOLD));
        }

        return builder;
    }

    /**
     * Invoke a private method via reflection.
     * Used to call setter methods that became private in 1.21.11.
     */
    private static void invokePrivate(Class<?> clazz, Object obj, String methodName, Class<?> paramType, Object value) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramType);
            method.setAccessible(true);
            method.invoke(obj, value);
        } catch (Exception ignored) {
            // Property not set - not critical
        }
    }
}
