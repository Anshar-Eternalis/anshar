package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.FrequencyIdentifier;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
    @Inject(at = @At("RETURN"), method = "updateLevel")
    private static void inUpdate(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir, @Local(index = 4) int i) {
        world.getPlayers().forEach(playerEntity -> {
            if (world.isClient()) return;
            if (i == 0) return;
            if (playerEntity.getPos().isInRange(new Vec3d(x, y, z), 8d)) {
                FrequencyIdentifier freq = new FrequencyIdentifier();
                freq.rescanPyramid(world, x, y, z, i);
                FrequencyIdentifier newfreq = new FrequencyIdentifier();
                NbtCompound tag = new NbtCompound();
                freq.writeToNbt(tag);
                newfreq.readFromNbt(tag);
                playerEntity.sendMessage(Text.literal(String.format("Beacon xyz: %d %d %d, frequency: %s %s",
                        x,
                        y,
                        z,
                        freq.arraysHashCode(),
                        newfreq.arraysHashCode()))
                );
            }
        });
    }
}
