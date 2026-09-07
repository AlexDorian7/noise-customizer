package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.Objects;

@Mixin(WorldGenRegion.class)
public class MixinWorldgenRegion {

    @Shadow @Final private ChunkAccess center;

    @Shadow @Final private ChunkStep generatingStep;

    @Shadow @Final private StaticCache2D<GenerationChunkHolder> cache;

    /**
     * @author
     * @reason
     */
    @Overwrite
    @Nullable
    public ChunkAccess getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
//        if (Math.abs(x) > 100_000_000 || Math.abs(z) > 100_000_000) {
//            System.out.println(
//                    "WorldGenRegion.getChunk: " +
//                            x + ", " + z +
//                            " status=" + chunkStatus +
//                            " require=" + requireChunk
//            );
//        }

        int i = wrappedChessboardDistance(this.center.getPos(), x, z);
        ChunkStatus chunkstatus = i >= this.generatingStep.directDependencies().size() ? null : this.generatingStep.directDependencies().get(i);
        GenerationChunkHolder generationchunkholder = null;
        if (chunkstatus != null) {
            try {
                generationchunkholder = this.cache.get(x, z);
                if (chunkStatus.isOrBefore(chunkstatus)) {
                    ChunkAccess chunkaccess = generationchunkholder.getChunkIfPresentUnchecked(chunkstatus);
                    if (chunkaccess != null) {
                        return chunkaccess;
                    }
                }
            } catch (IllegalStateException e) {
                throw new IllegalStateException(e);
            }
        } else {
            generationchunkholder = null;
        }

        if (!requireChunk) return null;

        if (generationchunkholder != null) {
            ChunkAccess chunkaccess = generationchunkholder.getChunkIfPresent(chunkstatus);
        }

        return center; // Fallback. Change this to be more proper later
    }

    @Unique
    private static int wrappedChessboardDistance(ChunkPos center, int x, int z) {
        long dx = Math.abs((long)center.x - x);
        long dz = Math.abs((long)center.z - z);

        if ((center.x >= 0) != (x >= 0)) {
            dx = Math.min(dx, (1L << 28) - dx);
        }

        if ((center.z >= 0) != (z >= 0)) {
            dz = Math.min(dz, (1L << 28) - dz);
        }

        return (int)Math.max(dx, dz);
    }

    @Unique
    private int getChessboardDistanceWrapped(int x, int z) {
        long dx = wrappedDistance(this.center.getPos().x, x);
        long dz = wrappedDistance(this.center.getPos().z, z);

        return (int)Math.max(dx, dz);
    }

    @Unique
    private static long wrappedDistance(int a, int b) {
        long distance = Math.abs((long)a - b);

        if ((a < 0) != (b < 0)) {
            distance = Math.min(distance, (1L << 32) - distance);
        }

        return distance;
    }
}
