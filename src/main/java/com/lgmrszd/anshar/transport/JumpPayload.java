package com.lgmrszd.anshar.transport;

import com.lgmrszd.anshar.beacon.BeaconNode;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;

public record JumpPayload(BlockPos blockPos, Text name, int color) implements CustomPayload {
    public static final Id<JumpPayload> ID = new Id<>(PlayerTransportComponent.JUMP_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, JumpPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, JumpPayload::blockPos,
            TextCodecs.PACKET_CODEC, JumpPayload::name,
            PacketCodecs.INTEGER, JumpPayload::color,
            JumpPayload::new
    );

    public static JumpPayload fromBeaconNode(BeaconNode node) {
        return new JumpPayload(
                node.getPos(),
                node.getName(),
                node.getColor()
        );
    }
    @Override
    public Id<? extends CustomPayload> getId() {
        return null;
    }
}
