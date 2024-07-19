package com.lgmrszd.anshar.payload.c2s;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public record JumpPayload(NbtCompound nbt) implements CustomPayload {
    public static final CustomPayload.Id<JumpPayload> ID = new Id<>(Identifier.of(MOD_ID, "player_transport_jump"));
    public static final PacketCodec<RegistryByteBuf, JumpPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, JumpPayload::nbt,
            JumpPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
