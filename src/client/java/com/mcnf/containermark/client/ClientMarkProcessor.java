package com.mcnf.containermark.client;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles marking in standalone mode (server mod not installed).
 * Uses client-side rendering + chat messages.
 */
public class ClientMarkProcessor {

    private static long lastMarkTick = 0;
    private static final int COOLDICK_TICKS = 40; // 2 seconds

    /**
     * Handle world mark (sneak + key while looking at container/item).
     */
    public static void handleWorldMark(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        long currentTick = client.world.getTime();
        if (currentTick - lastMarkTick < COOLDICK_TICKS) {
            client.player.sendMessage(Text.translatable("message.containermark.cooldown").formatted(Formatting.YELLOW), true);
            return;
        }

        HitResult hit = client.crosshairTarget;
        if (hit == null) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            BlockEntity be = client.world.getBlockEntity(pos);
            if (be instanceof Inventory inventory) {
                List<ItemStack> items = inspectInventory(inventory);
                // Use block name (chest, furnace, etc.) as the target name
                String targetName = client.world.getBlockState(pos).getBlock().getName().getString();
                executeMark(client, pos, items, targetName);
                lastMarkTick = currentTick;
            }
        } else if (hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getStack();
                if (!stack.isEmpty()) {
                    List<ItemStack> items = new ArrayList<>();
                    items.add(stack.copy());
                    executeMark(client, entity.getBlockPos(), items, stack.getName().getString());
                    lastMarkTick = currentTick;
                }
            }
        }
    }

    /**
     * Handle slot mark (key while hovering over item in container UI).
     */
    public static void handleSlotMark(MinecraftClient client, int slotIndex) {
        if (client.player == null || client.world == null) return;

        long currentTick = client.world.getTime();
        if (currentTick - lastMarkTick < COOLDICK_TICKS) {
            client.player.sendMessage(Text.translatable("message.containermark.cooldown").formatted(Formatting.YELLOW), true);
            return;
        }

        var screenHandler = client.player.currentScreenHandler;
        if (screenHandler == null) return;
        if (slotIndex < 0 || slotIndex >= screenHandler.slots.size()) return;

        var slot = screenHandler.getSlot(slotIndex);
        if (slot == null) return;

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) return;

        // For slot marks, use player position
        BlockPos pos = client.player.getBlockPos();
        List<ItemStack> items = new ArrayList<>();
        items.add(stack.copy());
        executeMark(client, pos, items, stack.getName().getString());
        lastMarkTick = currentTick;
    }

    /**
     * Execute the mark: floating text + particles + chat message.
     */
    private static void executeMark(MinecraftClient client, BlockPos pos, List<ItemStack> items, String targetName) {
        if (items.isEmpty()) {
            client.player.sendMessage(Text.translatable("message.containermark.empty").formatted(Formatting.YELLOW), true);
            return;
        }

        // 1. Client-side floating text (only visible to self)
        Text displayText = buildDisplayText(items);
        ClientDisplayRenderer.spawnDisplay(pos, displayText, 30);

        // 2. Client-side particles (only visible to self)
        spawnParticles(client, pos);

        // 3. Chat message (sent as public chat so teammates can see)
        MutableText msg = buildChatMessage(client.player.getName().getString(), targetName, pos, items);
        sendChatMessage(client, msg);
    }

    /**
     * Build display text for floating text.
     */
    private static Text buildDisplayText(List<ItemStack> items) {
        MutableText builder = Text.literal("");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) builder.append(Text.literal("\n"));
            ItemStack stack = items.get(i);
            builder.append(Text.literal(stack.getName().getString()).formatted(Formatting.WHITE));
            builder.append(Text.literal(" x" + stack.getCount()).formatted(Formatting.GOLD));
        }
        return builder;
    }

    /**
     * Build chat message with marker name, location, teleport link, and items.
     */
    private static MutableText buildChatMessage(String markerName, String targetName, BlockPos pos, List<ItemStack> items) {
        MutableText msg = Text.literal("");

        msg.append(Text.literal(markerName).formatted(Formatting.AQUA));
        msg.append(Text.literal(" 标记了 ").formatted(Formatting.WHITE));
        msg.append(Text.literal(targetName).formatted(Formatting.GOLD));

        String tpCommand = "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        msg.append(Text.literal("\n"));
        msg.append(Text.literal("位置: ").formatted(Formatting.GRAY));
        msg.append(Text.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] ").formatted(Formatting.WHITE));

        Style teleportStyle = Style.EMPTY
            .withClickEvent(new ClickEvent.RunCommand(tpCommand))
            .withHoverEvent(new HoverEvent.ShowText(Text.literal("点击传送")));
        msg.append(Text.literal("[传送]").setStyle(teleportStyle).formatted(Formatting.AQUA, Formatting.UNDERLINE));

        msg.append(Text.literal("\n"));
        msg.append(Text.literal("物品:").formatted(Formatting.GRAY));
        for (ItemStack item : items) {
            msg.append(Text.literal("\n"));
            msg.append(Text.literal("  ").append(item.getName()).formatted(Formatting.WHITE));
            msg.append(Text.literal(" x" + item.getCount()).formatted(Formatting.GOLD));
        }

        return msg;
    }

    /**
     * Send chat message. Uses sendChatMessage for public chat (teammates can see),
     * falls back to local message if chat is restricted.
     */
    private static void sendChatMessage(MinecraftClient client, Text msg) {
        try {
            // Try to send as public chat so teammates can see
            if (client.player != null) {
                client.inGameHud.getChatHud().addMessage(msg);
                // Also send to server as chat (teammates can see)
                // Note: This sends actual chat, may be filtered by server
                String plainText = msg.getString();
                if (plainText.length() <= 256 && client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendChatMessage(plainText);
                }
            }
        } catch (Exception e) {
            // Fallback: just show locally
            if (client.player != null) {
                client.player.sendMessage(msg, false);
            }
        }
    }

    /**
     * Spawn particles at position (client-side only).
     */
    private static void spawnParticles(MinecraftClient client, BlockPos pos) {
        if (client.world == null) return;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        for (int i = 0; i < 10; i++) {
            double dx = (client.world.random.nextDouble() - 0.5) * 1.5;
            double dy = client.world.random.nextDouble() * 2.0;
            double dz = (client.world.random.nextDouble() - 0.5) * 1.5;
            client.world.addParticleClient(
                net.minecraft.particle.ParticleTypes.END_ROD,
                x, y, z, dx * 0.1, dy * 0.05, dz * 0.1
            );
        }
    }

    /**
     * Inspect inventory and merge same items.
     */
    private static List<ItemStack> inspectInventory(Inventory inventory) {
        Map<net.minecraft.item.Item, ItemStack> merged = new LinkedHashMap<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            net.minecraft.item.Item item = stack.getItem();
            if (merged.containsKey(item)) {
                merged.get(item).increment(stack.getCount());
            } else {
                ItemStack copy = stack.copy();
                merged.put(item, copy);
            }
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * Get container name from BlockEntity.
     */
    private static String getContainerName(BlockEntity be) {
        try {
            return be.getCachedState().getBlock().getName().getString();
        } catch (Exception e) {
            return "Container";
        }
    }
}
