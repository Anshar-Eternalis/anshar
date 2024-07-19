package com.lgmrszd.anshar;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public class ModComponentTypes {
    public static final ComponentType<BlockPos> BEACON_POS = ComponentType.<BlockPos>builder().codec(BlockPos.CODEC).packetCodec(BlockPos.PACKET_CODEC).build();

    public static void register() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "beacon_pos"), BEACON_POS);
    }
}
