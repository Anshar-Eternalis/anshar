package com.lgmrszd.anshar.transport;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record EnterBeamPayload(BlockPos blockPos) implements CustomPayload {
    public static final Id<EnterBeamPayload> ID = new Id<>(BeaconComponent.ENTER_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, EnterBeamPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, EnterBeamPayload::blockPos,
            EnterBeamPayload::new
    );
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
