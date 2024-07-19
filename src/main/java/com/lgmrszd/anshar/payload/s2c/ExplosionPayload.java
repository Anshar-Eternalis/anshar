package com.lgmrszd.anshar.payload.s2c;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public record ExplosionPayload(BlockPos pos, int color) implements CustomPayload {
    public static final CustomPayload.Id<ExplosionPayload> ID = new Id<>(Identifier.of(MOD_ID, "player_transport_explosion"));
    public static final PacketCodec<RegistryByteBuf, ExplosionPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ExplosionPayload::pos,
            PacketCodecs.INTEGER, ExplosionPayload::color,
            ExplosionPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
