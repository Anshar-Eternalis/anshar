package com.lgmrszd.anshar.debug;

import net.minecraft.util.math.BlockPos;

public record DebugLine(BlockPos start, BlockPos end, int startColor, int endColor) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DebugLine other)) return false;
        if (!this.start.equals(other.start)) return false;
        if (!this.end.equals(other.end)) return false;
        if (this.startColor != other.startColor) return false;
        return this.endColor == other.endColor;
//        if (this.endColor != other.endColor) return false;
//        return this.lifetime == other.lifetime;
    }

    @Override
    public int hashCode() {
        int hash = start.hashCode() * 31 + end.hashCode();
        hash = hash * 31 + startColor;
        hash = hash * 31 + endColor;
        return hash;
//        return (start.hashCode() * 31 + end.hashCode()) * 31 + Long.hashCode(lifetime);
    }
}
