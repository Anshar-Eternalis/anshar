package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.BeaconComponent;
import com.lgmrszd.anshar.MyComponents;
import com.lgmrszd.anshar.freq.IBeaconComponent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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
        if (world.isClient()) return;
        if (i == 0) return;
        BlockEntity be = world.getBlockEntity(new BlockPos(x, y, z));
        if (!(be instanceof BeaconBlockEntity bbe)) return;
        IBeaconComponent freq = MyComponents.BEACON.get(bbe);
        world.getPlayers().forEach(playerEntity -> {
            if (playerEntity.getPos().isInRange(new Vec3d(x, y, z), 8d)) {
                freq.rescanPyramid(world, x, y, z, i);
                playerEntity.sendMessage(Text.literal(String.format("Beacon xyz: %d %d %d, frequency: %s",
                        x,
                        y,
                        z,
                        freq.arraysHashCode()))
                );
            }
        });
    }
}
