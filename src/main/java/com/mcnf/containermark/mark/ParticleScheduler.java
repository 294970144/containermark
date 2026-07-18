package com.mcnf.containermark.mark;

import com.mcnf.containermark.config.MarkConfig;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ParticleScheduler {
    private static final List<ParticleTask> tasks = new ArrayList<>();

    public static void schedule(ServerWorld world, BlockPos pos, long startTick) {
        long endTick = startTick + (long) MarkConfig.get().particleDurationSeconds * 20L;
        tasks.add(new ParticleTask(world.getRegistryKey(), pos, endTick));
    }

    public static void tick(MinecraftServer server) {
        if (tasks.isEmpty()) return;

        long currentTick = server.getTicks();
        tasks.removeIf(task -> {
            if (currentTick >= task.endTick) return true;

            // Spawn particles every 10 ticks (0.5 seconds)
            if (currentTick % 10 == 0) {
                ServerWorld world = server.getWorld(task.worldKey);
                if (world != null) {
                    double x = task.pos.getX() + 0.5;
                    double y = task.pos.getY() + 1.0;
                    double z = task.pos.getZ() + 0.5;
                    world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 8, 0.4, 0.4, 0.4, 0.03);
                }
            }
            return false;
        });
    }

    private static class ParticleTask {
        final RegistryKey<World> worldKey;
        final BlockPos pos;
        final long endTick;

        ParticleTask(RegistryKey<World> worldKey, BlockPos pos, long endTick) {
            this.worldKey = worldKey;
            this.pos = pos;
            this.endTick = endTick;
        }
    }
}
