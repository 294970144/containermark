package com.mcnf.containermark.target;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContainerInspector {

    /**
     * Inspect an inventory and return a merged list of items.
     * Same items are stacked together with total count.
     */
    public static List<ItemStack> inspectInventory(Inventory inventory) {
        Map<Item, ItemStack> merged = new LinkedHashMap<>();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (merged.containsKey(item)) {
                ItemStack existing = merged.get(item);
                existing.increment(stack.getCount());
            } else {
                // Create a copy to avoid modifying the original
                ItemStack copy = stack.copy();
                merged.put(item, copy);
            }
        }

        return new ArrayList<>(merged.values());
    }

    /**
     * Wrap a single item into a list.
     */
    public static List<ItemStack> singleItem(ItemStack stack) {
        List<ItemStack> list = new ArrayList<>();
        if (stack != null && !stack.isEmpty()) {
            list.add(stack.copy());
        }
        return list;
    }

    /**
     * Build a human-readable summary of items for display.
     */
    public static String buildSummary(List<ItemStack> items) {
        if (items.isEmpty()) return "Empty";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append("\n");
            ItemStack stack = items.get(i);
            Text name = stack.getName();
            sb.append(name.getString()).append(" x").append(stack.getCount());
        }
        return sb.toString();
    }

    /**
     * Get the display position for a container.
     * Returns the position above the container.
     */
    public static String getContainerName(Inventory inventory) {
        if (inventory == null) return "Container";
        // Try to get a friendly name from the inventory
        try {
            if (inventory instanceof net.minecraft.block.entity.BlockEntity be) {
                // Try to get the block name
                return be.getCachedState().getBlock().getName().getString();
            }
        } catch (Exception ignored) {}
        return "Container";
    }
}
