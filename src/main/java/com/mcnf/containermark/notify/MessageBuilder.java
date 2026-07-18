package com.mcnf.containermark.notify;

import com.mcnf.containermark.mark.MarkEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class MessageBuilder {

    public static Text buildMessage(MarkEntry entry, String targetName) {
        MutableText msg = Text.literal("");

        // Line 1: markerName marked targetName
        msg.append(Text.literal(entry.getMarkerName()).formatted(Formatting.AQUA));
        msg.append(Text.literal(" 标记了 ").formatted(Formatting.WHITE));
        msg.append(Text.literal(targetName).formatted(Formatting.GOLD));

        // Line 2: Location with teleport link
        BlockPos pos = entry.getPos();
        String tpCommand = "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ();

        msg.append(Text.literal("\n"));
        msg.append(Text.literal("位置: ").formatted(Formatting.GRAY));
        msg.append(Text.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] ").formatted(Formatting.WHITE));

        Style teleportStyle = Style.EMPTY
            .withClickEvent(new ClickEvent.RunCommand(tpCommand))
            .withHoverEvent(new HoverEvent.ShowText(Text.literal("点击传送")));
        msg.append(Text.literal("[传送]").setStyle(teleportStyle).formatted(Formatting.AQUA, Formatting.UNDERLINE));

        // Line 3: Dimension (if not overworld)
        String dimKey = entry.getDimensionKey();
        if (!dimKey.equals("minecraft:overworld")) {
            String dimName = dimKey.replace("minecraft:", "");
            msg.append(Text.literal("\n"));
            msg.append(Text.literal("维度: ").formatted(Formatting.GRAY));
            msg.append(Text.literal(dimName).formatted(Formatting.WHITE));
        }

        // Lines 4+: Items list
        msg.append(Text.literal("\n"));
        msg.append(Text.literal("物品:").formatted(Formatting.GRAY));

        for (ItemStack item : entry.getItems()) {
            msg.append(Text.literal("\n"));
            msg.append(Text.literal("  ").append(item.getName()).formatted(Formatting.WHITE));
            msg.append(Text.literal(" x" + item.getCount()).formatted(Formatting.GOLD));
        }

        return msg;
    }
}
