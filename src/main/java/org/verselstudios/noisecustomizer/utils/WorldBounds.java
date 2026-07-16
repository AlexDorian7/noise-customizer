package org.verselstudios.noisecustomizer.utils;

public final class WorldBounds {
    private static final long LIMIT = (long) Integer.MAX_VALUE + 16; // allow for one chunk beyone to be generated. world border should prevent passing

    public static boolean isChunkAllowed(int chunkX, int chunkZ) {
        long minX = ((long) chunkX) << 4;
        long maxX = minX + 15;

        long minZ = ((long) chunkZ) << 4;
        long maxZ = minZ + 15;

        return minX >= -LIMIT
                && maxX <= LIMIT
                && minZ >= -LIMIT
                && maxZ <= LIMIT;
    }
}