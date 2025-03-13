package com.lgmrszd.anshar.transport;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;

public record ExplosionPayload(BlockPos blockPos, int color) implements CustomPayload {
    public static final CustomPayload.Id<ExplosionPayload> ID = new CustomPayload.Id<>(PlayerTransportComponent.EXPLOSION_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, ExplosionPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ExplosionPayload::blockPos,
            PacketCodecs.INTEGER, ExplosionPayload::color,
            ExplosionPayload::new
    );
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
