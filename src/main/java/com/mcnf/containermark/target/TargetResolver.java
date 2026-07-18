package com.mcnf.containermark.target;

import com.mcnf.containermark.network.MarkWorldPayload;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TargetResolver {

    private static final double MAX_REACH = 6.0;

    /**
     * Resolve a world mark target (block container or item entity).
     * Returns null if validation fails.
     */
    public static ResolvedTarget resolveWorldTarget(ServerPlayerEntity player, MarkWorldPayload payload) {
        ServerWorld world = player.getEntityWorld();

        // Verify dimension matches
        if (!world.getRegistryKey().getValue().equals(payload.dimension())) {
            return null;
        }

        if (payload.targetType() == MarkWorldPayload.TYPE_BLOCK) {
            BlockPos pos = payload.pos();
            // Distance check
            double dist = player.getEntityPos().distanceTo(Vec3d.ofCenter(pos));
            if (dist > MAX_REACH) return null;

            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof Inventory inventory) {
                return new ResolvedTarget(pos, inventory, null);
            }
            return null;

        } else if (payload.targetType() == MarkWorldPayload.TYPE_ITEM_ENTITY) {
            Entity entity = world.getEntityById(payload.entityId());
            if (!(entity instanceof ItemEntity itemEntity)) return null;

            // Distance check
            double dist = player.getEntityPos().distanceTo(entity.getEntityPos());
            if (dist > MAX_REACH) return null;

            ItemStack stack = itemEntity.getStack();
            if (stack.isEmpty()) return null;

            return new ResolvedTarget(entity.getBlockPos(), null, stack);
        }

        return null;
    }

    /**
     * Resolve a slot mark target (item in a container UI).
     * Returns null if validation fails.
     */
    public static ItemStack resolveSlotTarget(ServerPlayerEntity player, int slotIndex) {
        // Check if the slot index is valid in the player's current screen handler
        var screenHandler = player.currentScreenHandler;
        if (screenHandler == null) return null;
        if (slotIndex < 0 || slotIndex >= screenHandler.slots.size()) return null;

        var slot = screenHandler.getSlot(slotIndex);
        if (slot == null) return null;

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) return null;

        return stack;
    }

    public record ResolvedTarget(BlockPos pos, Inventory inventory, ItemStack singleItem) {
        public boolean isContainer() {
            return inventory != null;
        }

        public boolean isItemEntity() {
            return singleItem != null;
        }
    }
}
