package com.mcnf.containermark.network;

import com.mcnf.containermark.ContainerMark;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MarkSlotPayload(int slotIndex) implements CustomPayload {
    public static final CustomPayload.Id<MarkSlotPayload> ID =
        new CustomPayload.Id<>(Identifier.of(ContainerMark.MOD_ID, "mark_slot"));

    public static final PacketCodec<RegistryByteBuf, MarkSlotPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.VAR_INT, MarkSlotPayload::slotIndex,
            MarkSlotPayload::new
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
