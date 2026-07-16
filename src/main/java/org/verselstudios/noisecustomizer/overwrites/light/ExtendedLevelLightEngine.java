package org.verselstudios.noisecustomizer.overwrites.light;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.*;
import net.minecraft.world.level.lighting.LayerLightEventListener.DummyLightLayerEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage.SectionType;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public class ExtendedLevelLightEngine extends LevelLightEngine implements LightEventListener {
    public static final int LIGHT_SECTION_PADDING = 1;
    protected final LevelHeightAccessor levelHeightAccessor;
    @Nullable
    private final ExtendedLightEngine<?, ?> blockEngine;
    @Nullable
    private final ExtendedLightEngine<?, ?> skyEngine;

    public ExtendedLevelLightEngine(LightChunkGetter lightChunkGetter, boolean blockLight, boolean skyLight) {
        super(lightChunkGetter, blockLight, skyLight);
        this.levelHeightAccessor = lightChunkGetter.getLevel();
        this.blockEngine = blockLight ? new ExtendedBlockLightEngine(lightChunkGetter) : null;
        this.skyEngine = skyLight ? new ExtendedSkyLightEngine(lightChunkGetter) : null;
    }

    public void checkBlock(BlockPos pos) {
        if (this.blockEngine != null) {
            this.blockEngine.checkBlock(pos);
        }

        if (this.skyEngine != null) {
            this.skyEngine.checkBlock(pos);
        }

    }

    public boolean hasLightWork() {
        return this.skyEngine != null && this.skyEngine.hasLightWork() ? true : this.blockEngine != null && this.blockEngine.hasLightWork();
    }

    public int runLightUpdates() {
        int i = 0;
        if (this.blockEngine != null) {
            i += this.blockEngine.runLightUpdates();
        }

        if (this.skyEngine != null) {
            i += this.skyEngine.runLightUpdates();
        }

        return i;
    }

    public void updateSectionStatus(SectionPos pos, boolean isEmpty) {
        if (this.blockEngine != null) {
            this.blockEngine.updateSectionStatus(pos, isEmpty);
        }

        if (this.skyEngine != null) {
            this.skyEngine.updateSectionStatus(pos, isEmpty);
        }

    }

    public void setLightEnabled(ChunkPos chunkPos, boolean lightEnabled) {
        if (this.blockEngine != null) {
            this.blockEngine.setLightEnabled(chunkPos, lightEnabled);
        }

        if (this.skyEngine != null) {
            this.skyEngine.setLightEnabled(chunkPos, lightEnabled);
        }

    }

    public void propagateLightSources(ChunkPos chunkPos) {
        if (this.blockEngine != null) {
            this.blockEngine.propagateLightSources(chunkPos);
        }

        if (this.skyEngine != null) {
            this.skyEngine.propagateLightSources(chunkPos);
        }

    }

    public LayerLightEventListener getLayerListener(LightLayer type) {
        if (type == LightLayer.BLOCK) {
            return this.blockEngine == null ? DummyLightLayerEventListener.INSTANCE : this.blockEngine;
        } else {
            return this.skyEngine == null ? DummyLightLayerEventListener.INSTANCE : this.skyEngine;
        }
    }

    public String getDebugData(LightLayer lightLayer, SectionPos sectionPos) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugData(CoordinateKey.fromSection(sectionPos));
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugData(CoordinateKey.fromSection(sectionPos));
        }

        return "n/a";
    }

    public LayerLightSectionStorage.SectionType getDebugSectionType(LightLayer lightLayer, SectionPos sectionPos) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugSectionType(CoordinateKey.fromSection(sectionPos)).convert();
            }
        } else if (this.skyEngine != null) {
            return this.skyEngine.getDebugSectionType(CoordinateKey.fromSection(sectionPos)).convert();
        }

        return SectionType.EMPTY;
    }

    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                this.blockEngine.queueSectionData(CoordinateKey.fromSection(sectionPos), dataLayer);
            }
        } else if (this.skyEngine != null) {
            this.skyEngine.queueSectionData(CoordinateKey.fromSection(sectionPos), dataLayer);
        }

    }

    public void retainData(ChunkPos pos, boolean retain) {
        if (this.blockEngine != null) {
            this.blockEngine.retainData(pos, retain);
        }

        if (this.skyEngine != null) {
            this.skyEngine.retainData(pos, retain);
        }

    }

    public int getRawBrightness(BlockPos blockPos, int amount) {
        int i = this.skyEngine == null ? 0 : this.skyEngine.getLightValue(blockPos) - amount;
        int j = this.blockEngine == null ? 0 : this.blockEngine.getLightValue(blockPos);
        return Math.max(j, i);
    }

    public boolean lightOnInSection(SectionPos sectionPos) {
        CoordinateKey i = CoordinateKey.fromSection(sectionPos);
        return this.blockEngine == null || this.blockEngine.storage.lightOnInSection(i) && (this.skyEngine == null || this.skyEngine.storage.lightOnInSection(i));
    }

    public int getLightSectionCount() {
        return this.levelHeightAccessor.getSectionsCount() + 2;
    }

    public int getMinLightSection() {
        return this.levelHeightAccessor.getMinSection() - 1;
    }

    public int getMaxLightSection() {
        return this.getMinLightSection() + this.getLightSectionCount();
    }
}
