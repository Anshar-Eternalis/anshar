package com.lgmrszd.anshar.storage;

import static com.lgmrszd.anshar.Anshar.LOGGER;
import static com.lgmrszd.anshar.Anshar.MOD_ID;

import com.lgmrszd.anshar.network.Network;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.UUID;

public class LevelStorageComponent implements Component {
    public static final ComponentKey<LevelStorageComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "level_storage"), LevelStorageComponent.class
    );

    private HashMap<UUID, Network> networks;

    public LevelStorageComponent(){
        networks = new HashMap<>();
        LOGGER.debug("Level (save) storage component test");
    }

//    public

    @Override
    public void readFromNbt(NbtCompound tag) {
        NbtCompound networksTag = tag.getCompound("networks");
        for (String uuid_string: networksTag.getKeys()) {
            UUID uuid = UUID.fromString(uuid_string);
            Network network = new Network(uuid);
            NbtCompound networkTag = networksTag.getCompound(uuid_string);
            network.readFromNbt(networkTag);
//            if (network.getId() != uuid) {
//                LOGGER.warn("Error adding network: stored key ");
//            }
            networks.put(uuid, network);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtCompound networksTag = new NbtCompound();
        networks.forEach((uuid, network) -> {
            NbtCompound networkTag = new NbtCompound();
            network.writeToNbt(networkTag);
            networksTag.put(uuid.toString(), networkTag);
        });
        tag.put("networks", networksTag);
    }
}
