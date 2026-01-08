package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

@Mixin(WorldGenRegion.class)
public class MixinWorldgenRegion {

    @Shadow
    @Final
    private ChunkAccess center;

    @Shadow
    @Final
    private ChunkStep generatingStep;

    @Shadow
    @Final
    private StaticCache2D<GenerationChunkHolder> cache;

    /**
     * @author Versel
     * @reason Fix Structures
     */
    @Overwrite
    @Nullable
    public ChunkAccess getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
        int i = this.center.getPos().getChessboardDistance(x, z);
        /*
        I feel like this probably breaks generation in some ways...
        It does at least fix the server crash when trying to generate some structures at large distances from spawn.
         */
        if (i >= this.generatingStep.directDependencies().size()) {
            i = 0;
            x = this.center.getPos().x;
            z = this.center.getPos().z;
        }
        ChunkStatus chunkstatus = i >= this.generatingStep.directDependencies().size() ? null : this.generatingStep.directDependencies().get(i);
        GenerationChunkHolder generationchunkholder;
        if (chunkstatus != null) {
            generationchunkholder = this.cache.get(x, z);
            if (chunkStatus.isOrBefore(chunkstatus)) {
                ChunkAccess chunkaccess = generationchunkholder.getChunkIfPresentUnchecked(chunkstatus);
                if (chunkaccess != null) {
                    return chunkaccess;
                }
            }
        } else {
            generationchunkholder = null;
        }

        CrashReport crashreport = CrashReport.forThrowable(new IllegalStateException("Requested chunk unavailable during world generation"), "Exception generating new chunk");
        CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk request details");
        crashreportcategory.setDetail("Requested chunk", String.format(Locale.ROOT, "%d, %d", x, z));
        crashreportcategory.setDetail("Generating status", () -> {
            return this.generatingStep.targetStatus().getName();
        });
        Objects.requireNonNull(chunkStatus);
        crashreportcategory.setDetail("Requested status", chunkStatus::getName);
        crashreportcategory.setDetail("Actual status", () -> {
            return generationchunkholder == null ? "[out of cache bounds]" : generationchunkholder.getPersistedStatus().getName();
        });
        crashreportcategory.setDetail("Maximum allowed status", () -> {
            return chunkstatus == null ? "null" : chunkstatus.getName();
        });
        ChunkDependencies var10002 = this.generatingStep.directDependencies();
        Objects.requireNonNull(var10002);
        crashreportcategory.setDetail("Dependencies", var10002::toString);
        crashreportcategory.setDetail("Requested distance", i);
        ChunkPos var10 = this.center.getPos();
        Objects.requireNonNull(var10);
        crashreportcategory.setDetail("Generating chunk", var10::toString);
        throw new ReportedException(crashreport);
    }
}
