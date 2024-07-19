package com.lgmrszd.anshar.payload.c2s;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public record EnterPayload(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<EnterPayload> ID = new Id<>(Identifier.of(MOD_ID, "player_transport_enter"));
    public static final PacketCodec<RegistryByteBuf, EnterPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, EnterPayload::pos,
            EnterPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
