package com.mcnf.containermark.network;

import com.mcnf.containermark.ContainerMark;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record MarkWorldPayload(int targetType, BlockPos pos, int entityId, Identifier dimension) implements CustomPayload {
    public static final int TYPE_BLOCK = 0;
    public static final int TYPE_ITEM_ENTITY = 1;

    public static final CustomPayload.Id<MarkWorldPayload> ID =
        new CustomPayload.Id<>(Identifier.of(ContainerMark.MOD_ID, "mark_world"));

    public static final PacketCodec<RegistryByteBuf, MarkWorldPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.INTEGER, MarkWorldPayload::targetType,
            BlockPos.PACKET_CODEC, MarkWorldPayload::pos,
            PacketCodecs.VAR_INT, MarkWorldPayload::entityId,
            Identifier.PACKET_CODEC, MarkWorldPayload::dimension,
            MarkWorldPayload::new
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
