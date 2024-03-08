package com.lgmrszd.anshar;

import com.lgmrszd.anshar.mixin.accessor.BeaconBlockEntityAccessor;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class AnsharUtil {
    public static EndCrystalEntity lookUpEndCrystal(World world, BlockPos pos) {
        Vec3d scanPos = pos.toCenterPos().add(0, -0.5, 0);
        double d = scanPos.getX();
        double e = scanPos.getY();
        double f = scanPos.getZ();
        List<Entity> list = world.getOtherEntities(null, new Box(d-0.1, e, f-0.1,
                d+0.1, e+0.2, f+0.1));
        if (!list.isEmpty()) {
            for (Entity entity: list) {
                if (!(entity instanceof EndCrystalEntity ece)) continue;
                if (entity.getPos().relativize(scanPos).length() > 0.1) continue;
                return ece;
            }
        }
        return null;
    }

    public static BlockPos getBeaconForBlock(World world, BlockPos pos) {
        List<BlockPos> nearbyBeacons = new ArrayList<>();
        int x1 = pos.getX() - 4;
        int y1 = pos.getY() - 4;
        int z1 = pos.getZ() - 4;
        int x2 = x1 + 9;
        int y2 = y1 + 4;
        int z2 = x1 + 9;
        for (int x = x1; x <= x2; x++)
            for (int y = y1; y <= y2; y++)
                for (int z = z1; z <= z2; z++) {
                    BlockPos targetPos = new BlockPos(x, y, z);
                    if (!(world.getBlockEntity(targetPos) instanceof BeaconBlockEntity beaconBlockEntity)) continue;
                    if (isBeaconValidTarget(pos, beaconBlockEntity)) nearbyBeacons.add(targetPos);
                }
        // TODO: add sorting to nearest
        nearbyBeacons.sort((o1, o2) -> MathHelper.floor(o1.getSquaredDistance(pos)) - MathHelper.floor(o2.getSquaredDistance(pos)));
        if (!nearbyBeacons.isEmpty()) return nearbyBeacons.get(0);
        return null;
    }

    public static boolean isBeaconValidTarget(BlockPos pos, BeaconBlockEntity beacon){
        var diff = beacon.getPos().subtract(pos);
        var tier = diff.getY() + 1;
        return tier <= ((BeaconBlockEntityAccessor)beacon).getLevel() && (
                (tier == Math.abs(diff.getX()) && Math.abs(diff.getZ()) <= tier) ||
                        (tier == Math.abs(diff.getZ()) && Math.abs(diff.getX()) <= tier)
        );
    }

    public enum LinkedState {
        UNLINKED(new Vector3f(1f, 0, 0)),
        LINKED_PYRAMID(new Vector3f(0, 0, 1f)),
        LINKED_CRYSTAL(new Vector3f(1f, 1f, 1f));
        private final Vector3f color;

        LinkedState(Vector3f color) {
            this.color = color;
        }

        public Vector3f getColor() {
            return color;
        }
    }
}
