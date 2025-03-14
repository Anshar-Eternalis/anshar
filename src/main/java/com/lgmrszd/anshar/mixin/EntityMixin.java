package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow @Final protected DataTracker dataTracker;

    @Shadow @Final private static TrackedData<Boolean> NO_GRAVITY;

    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    public void anshar$onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {

    }

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void invisibleWhileInNetwork(CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player && PlayerTransportComponent.KEY.get(player).isInNetwork()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasNoGravity", at = @At("HEAD"), cancellable = true)
    private void noGravityWhileInNetwork(CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof PlayerEntity player && PlayerTransportComponent.KEY.get(player).isInNetwork()) {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasNoGravity()Z"))
    private boolean doNotWriteCustomNoGravityToNbt(boolean original) {
        return this.dataTracker.get(NO_GRAVITY);
    }
}
