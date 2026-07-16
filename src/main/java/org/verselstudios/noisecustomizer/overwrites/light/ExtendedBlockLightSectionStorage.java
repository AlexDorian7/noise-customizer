package org.verselstudios.noisecustomizer.overwrites.light;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public class ExtendedBlockLightSectionStorage extends ExtendedLayerLightSectionStorage<ExtendedBlockLightSectionStorage.ExtendedBlockDataLayerStorageMap> {
    protected ExtendedBlockLightSectionStorage(LightChunkGetter chunkSource) {
        super(LightLayer.BLOCK, chunkSource, new ExtendedBlockLightSectionStorage.ExtendedBlockDataLayerStorageMap(new Object2ObjectOpenHashMap<>()));
    }

    @Override
    protected int getLightValue(CoordinateKey levelPos) {
        CoordinateKey sectionPos = levelPos.toSection();
        DataLayer datalayer = this.getDataLayer(sectionPos, false);
        CoordinateKey rel = levelPos.sectionRelative();
        return datalayer == null
                ? 0
                : datalayer.get(
                rel.x(),
                rel.y(),
                rel.z()
        );
    }

    protected static final class ExtendedBlockDataLayerStorageMap extends ExtendedDataLayerStorageMap<ExtendedBlockLightSectionStorage.ExtendedBlockDataLayerStorageMap> {
        public ExtendedBlockDataLayerStorageMap(Object2ObjectOpenHashMap<CoordinateKey, DataLayer> map) {
            super(map);
        }

        public ExtendedBlockDataLayerStorageMap copy() {
            return new ExtendedBlockDataLayerStorageMap(this.map.clone());
        }
    }
}
