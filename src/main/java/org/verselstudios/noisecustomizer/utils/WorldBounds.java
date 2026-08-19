package org.verselstudios.noisecustomizer.utils;

public final class WorldBounds {
    public static final int MAX_CHUNK = Integer.MAX_VALUE / 16;
    public static final int MIN_CHUNK = Integer.MIN_VALUE / 16;
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