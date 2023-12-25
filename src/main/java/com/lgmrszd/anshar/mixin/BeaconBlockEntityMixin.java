package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {
// TODO: maybe there's a way for components to listen to removal events, but haven't found any, so here's a mixin
    @Inject(at = @At("RETURN"), method = "markRemoved")
    public void removeFromNetworkInMarkRemoved(CallbackInfo ci) {
        BeaconBlockEntity bbe = (BeaconBlockEntity) (Object) this;
        // TODO: rewrite this with new NetworkManagerComponent code later
        BeaconComponent.KEY.get(bbe).getFrequencyNetwork().ifPresent(frequencyNetwork -> {
            frequencyNetwork.getBeacons().remove(bbe.getPos());
        });
    }
}
