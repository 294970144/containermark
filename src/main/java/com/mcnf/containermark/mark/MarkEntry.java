package com.mcnf.containermark.mark;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class MarkEntry {
    public enum TargetType { BLOCK, ITEM_ENTITY, SLOT }

    private final TargetType targetType;
    private final BlockPos pos;
    private final String markerName;
    private final List<ItemStack> items;
    private final String dimensionKey;
    private final long createdAtTick;

    public MarkEntry(TargetType targetType, BlockPos pos, String markerName, List<ItemStack> items, String dimensionKey, long createdAtTick) {
        this.targetType = targetType;
        this.pos = pos;
        this.markerName = markerName;
        this.items = items;
        this.dimensionKey = dimensionKey;
        this.createdAtTick = createdAtTick;
    }

    public TargetType getTargetType() { return targetType; }
    public BlockPos getPos() { return pos; }
    public String getMarkerName() { return markerName; }
    public List<ItemStack> getItems() { return items; }
    public String getDimensionKey() { return dimensionKey; }
    public long getCreatedAtTick() { return createdAtTick; }
}
