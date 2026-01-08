package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;

@Mixin(LevelReader.class)
public interface MixinLevelReader extends BlockAndTintGetter {

    /**
     * @author Versel
     * @reason To remove 30 mil border
     */
    @Overwrite
    default int getMaxLocalRawBrightness(BlockPos pos, int amount) {
        return this.getRawBrightness(pos, amount);
    }

    /**
     * @author Versel
     * @reason Debugging
     */
    @Overwrite
    default Holder<Biome> getNoiseBiome(int x, int y, int z) {
        try {
            ChunkAccess chunkaccess = this.getChunk(QuartPos.toSection(x), QuartPos.toSection(z), ChunkStatus.BIOMES, false);
            return chunkaccess != null ? chunkaccess.getNoiseBiome(x, y, z) : this.getUncachedNoiseBiome(x, y, z);
        } catch (Throwable t) {
            CrashReport crashReport = CrashReport.forThrowable(t, "Versel Debug Code");
            CrashReportCategory category = crashReport.addCategory("VERSEL");
            category.setDetail("Given chunk", String.format(Locale.ROOT, "%d, %d", x, z));
            throw new ReportedException(crashReport);
        }
    }

    @Shadow
    Holder<Biome> getUncachedNoiseBiome(int x, int y, int z);

    @Shadow
    ChunkAccess getChunk(int section, int section1, ChunkStatus biomes, boolean b);
}
