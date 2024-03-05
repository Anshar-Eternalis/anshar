package com.lgmrszd.anshar.storage;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.IBeaconComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


import java.lang.ref.WeakReference;
import java.util.Optional;

import static com.lgmrszd.anshar.Anshar.LOGGER;
import static com.lgmrszd.anshar.Anshar.MOD_ID;

public class EnderChestComponent implements ServerTickingComponent {
    public static final ComponentKey<EnderChestComponent> KEY = ComponentRegistry.getOrCreate(
            new Identifier(MOD_ID, "ender_chest"), EnderChestComponent.class
    );

    private final EnderChestBlockEntity enderChestBlockEntity;

    private EmbeddedStorage linkedStorage;
    private BlockPos linkedBeacon;
    private boolean linked;
//    private boolean active;

    public EnderChestComponent(EnderChestBlockEntity enderChestBlockEntity) {
        this.enderChestBlockEntity = enderChestBlockEntity;
        linkedStorage = null;
    }

    @Override
    public void serverTick() {
        if (!(enderChestBlockEntity.getWorld() instanceof ServerWorld world)) return;
        keepAliveConnections();
        // big delay for debug purposes for now
        if (world.getTime() % 100 == 0) {
            refreshConnections();

            EmbeddedStorage.getForEnderChestBlockEntity(enderChestBlockEntity).ifPresentOrElse(embeddedStorage -> {
                linkedStorage = embeddedStorage;
            }, () -> linkedStorage = null);
        }
//        if (linkedStorage == null) return;
//        if (world.getTime() % 20 != 0) return;
        Vec3d centralPos = enderChestBlockEntity.getPos().toCenterPos();
        world.spawnParticles(
                linked ? ParticleTypes.GLOW : ParticleTypes.DRIPPING_LAVA,
                centralPos.x,
                centralPos.y+0.7,
                centralPos.z,
                4, 0, 0.5, 0, 0
        );
    }

    private void refreshConnections() {
        EmbeddedStorage.getConnectedBeacon(
                enderChestBlockEntity.getWorld(),
                enderChestBlockEntity.getPos(),
                enderChestBlockEntity
        ).ifPresentOrElse(
                beaconBlockEntity -> {
                    IBeaconComponent beaconComponent = BeaconComponent.KEY.get(beaconBlockEntity);
                    BlockPos beaconPos = beaconComponent.getBeaconPos();
                    if (beaconPos.equals(linkedBeacon)) return;
                    linkedBeacon = beaconPos;
                    linked = true;
                    LOGGER.info("got linked! (manual check)");
                },
                () -> {
                    if (!linked) return;
                    linkedBeacon = null;
                    linked = false;
                    LOGGER.info("got unlinked! (manual check)");
                }
        );
    }

    private void keepAliveConnections() {
        if (linkedBeacon != null && enderChestBlockEntity.getWorld() != null) {
            if (enderChestBlockEntity.getWorld().getBlockEntity(linkedBeacon) instanceof BeaconBlockEntity) return;
            linked = false;
            linkedBeacon = null;
            linkedStorage = null;
            LOGGER.info("got unlinked! (Beacon Block removed)");
        }
    }

    public boolean tryOpen(ServerPlayerEntity player) {
        if (linkedStorage == null) return false;
        linkedStorage.setActiveBlockEntity(enderChestBlockEntity);
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
              (syncId, playerInv, playerx) ->
                      GenericContainerScreenHandler.createGeneric9x3(syncId, playerInv, linkedStorage),
                linkedStorage.getContainerLabelFor(null)));
        player.incrementStat(Stats.OPEN_ENDERCHEST);
        return true;
    }

    public Optional<EmbeddedStorage> getLinkedStorage() {
        return Optional.ofNullable(linkedStorage);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

    }
}
