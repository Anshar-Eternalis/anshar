package com.lgmrszd.anshar.storage;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class LevelStorageComponent implements Component {
    public static final ComponentKey<LevelStorageComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "level_storage"), LevelStorageComponent.class
    );

    public LevelStorageComponent(){
        System.out.println("Level (save) storage component test");
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        
    }
}
