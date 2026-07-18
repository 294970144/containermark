package com.mcnf.containermark.mark;

import com.mcnf.containermark.ContainerMark;
import com.mcnf.containermark.config.MarkConfig;
import com.mcnf.containermark.network.MarkWorldPayload;
import com.mcnf.containermark.network.MarkSlotPayload;
import com.mcnf.containermark.notify.MessageBuilder;
import com.mcnf.containermark.notify.PlayerSelector;
import com.mcnf.containermark.target.ContainerInspector;
import com.mcnf.containermark.target.TargetResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MarkHandler {

    private static final Map<UUID, Long> lastMarkTick = new HashMap<>();

    public static void handleWorldMark(ServerPlayerEntity player, MarkWorldPayload payload) {
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;

        long currentTick = server.getTicks();

        // Cooldown check
        if (!checkCooldown(player, currentTick)) return;

        // Resolve target
        TargetResolver.ResolvedTarget target = TargetResolver.resolveWorldTarget(player, payload);
        if (target == null) {
            player.sendMessage(Text.translatable("message.containermark.empty").formatted(Formatting.RED), false);
            return;
        }

        // Get items
        List<ItemStack> items;
        String containerName;

        if (target.isContainer()) {
            items = ContainerInspector.inspectInventory(target.inventory());
            containerName = ContainerInspector.getContainerName(target.inventory());
            if (items.isEmpty()) {
                player.sendMessage(Text.translatable("message.containermark.empty").formatted(Formatting.RED), false);
                return;
            }
        } else {
            items = ContainerInspector.singleItem(target.singleItem());
            containerName = target.singleItem().getName().getString();
        }

        // Create mark entry
        MarkEntry entry = new MarkEntry(
            target.isContainer() ? MarkEntry.TargetType.BLOCK : MarkEntry.TargetType.ITEM_ENTITY,
            target.pos(),
            player.getName().getString(),
            items,
            player.getEntityWorld().getRegistryKey().getValue().toString(),
            currentTick
        );

        // Execute three notifications
        ServerWorld world = player.getEntityWorld();

        // 1. Floating text display
        DisplayManager.spawnDisplay(world, target.pos(), items, player.getUuid(), currentTick);

        // 2. Particles
        if (MarkConfig.get().particleEnabled) {
            spawnParticles(world, target.pos(), currentTick);
        }

        // 3. Chat message to teammates
        List<ServerPlayerEntity> targets = PlayerSelector.selectPlayers(server, player, target.pos());
        Text message = MessageBuilder.buildMessage(entry, containerName);
        for (ServerPlayerEntity p : targets) {
            p.sendMessage(message, false);
        }

        // Update cooldown
        lastMarkTick.put(player.getUuid(), currentTick);
    }

    public static void handleSlotMark(ServerPlayerEntity player, MarkSlotPayload payload) {
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;

        long currentTick = server.getTicks();

        // Cooldown check
        if (!checkCooldown(player, currentTick)) return;

        // Resolve slot target
        ItemStack stack = TargetResolver.resolveSlotTarget(player, payload.slotIndex());
        if (stack == null || stack.isEmpty()) {
            player.sendMessage(Text.translatable("message.containermark.empty").formatted(Formatting.RED), false);
            return;
        }

        // Create items list
        List<ItemStack> items = ContainerInspector.singleItem(stack);
        String itemName = stack.getName().getString();

        // Get player position as the mark location
        BlockPos pos = player.getBlockPos();

        // Create mark entry
        MarkEntry entry = new MarkEntry(
            MarkEntry.TargetType.SLOT,
            pos,
            player.getName().getString(),
            items,
            player.getEntityWorld().getRegistryKey().getValue().toString(),
            currentTick
        );

        // For slot marks, only send chat message (no display/particles at player position)
        List<ServerPlayerEntity> targets = PlayerSelector.selectPlayers(server, player, pos);
        Text message = MessageBuilder.buildMessage(entry, itemName);
        for (ServerPlayerEntity p : targets) {
            p.sendMessage(message, false);
        }

        // Update cooldown
        lastMarkTick.put(player.getUuid(), currentTick);
    }

    private static boolean checkCooldown(ServerPlayerEntity player, long currentTick) {
        Long last = lastMarkTick.get(player.getUuid());
        if (last != null) {
            long elapsedTicks = currentTick - last;
            long cooldownTicks = MarkConfig.get().markCooldownSeconds * 20L;
            if (elapsedTicks < cooldownTicks) {
                player.sendMessage(Text.translatable("message.containermark.cooldown").formatted(Formatting.YELLOW), false);
                return false;
            }
        }
        return true;
    }

    private static void spawnParticles(ServerWorld world, BlockPos pos, long currentTick) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        // Initial burst
        world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 15, 0.5, 0.5, 0.5, 0.05);

        // Schedule continuous particles via tick-based approach
        ParticleScheduler.schedule(world, pos, currentTick);
    }
}
