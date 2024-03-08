package com.lgmrszd.anshar.debug;

import net.minecraft.util.math.BlockPos;

public record DebugLine(BlockPos start, BlockPos end, long lifetime) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DebugLine other)) return false;
        return
                this.start.equals(other.start) &&
                        this.end.equals(other.end) &&
                        this.lifetime == other.lifetime;
    }

    @Override
    public int hashCode() {
        return (start.hashCode() * 31 + end.hashCode()) * 31 + Long.hashCode(lifetime);
    }
}
