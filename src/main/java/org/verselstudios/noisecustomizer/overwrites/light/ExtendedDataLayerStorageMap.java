package org.verselstudios.noisecustomizer.overwrites.light;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.chunk.DataLayer;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public abstract class ExtendedDataLayerStorageMap<M extends ExtendedDataLayerStorageMap<M>> {
    private static final int CACHE_SIZE = 2;
    private final CoordinateKey[] lastSectionKeys = new CoordinateKey[2];
    private final DataLayer[] lastSections = new DataLayer[2];
    private boolean cacheEnabled;
    protected final Object2ObjectOpenHashMap<CoordinateKey, DataLayer> map;

    protected ExtendedDataLayerStorageMap(Object2ObjectOpenHashMap<CoordinateKey, DataLayer> map) {
        this.map = map;
        this.clearCache();
        this.cacheEnabled = true;
    }

    public abstract M copy();

    public DataLayer copyDataLayer(CoordinateKey index) {
        DataLayer datalayer = this.map.get(index).copy();
        this.map.put(index, datalayer);
        this.clearCache();
        return datalayer;
    }

    public boolean hasLayer(CoordinateKey sectionPos) {
        return this.map.containsKey(sectionPos);
    }

    @Nullable
    public DataLayer getLayer(CoordinateKey sectionPos) {
        if (this.cacheEnabled) {
            for (int i = 0; i < 2; i++) {
                if (sectionPos == this.lastSectionKeys[i]) {
                    return this.lastSections[i];
                }
            }
        }

        DataLayer datalayer = this.map.get(sectionPos);
        if (datalayer == null) {
            return null;
        } else {
            if (this.cacheEnabled) {
                for (int j = 1; j > 0; j--) {
                    this.lastSectionKeys[j] = this.lastSectionKeys[j - 1];
                    this.lastSections[j] = this.lastSections[j - 1];
                }

                this.lastSectionKeys[0] = sectionPos;
                this.lastSections[0] = datalayer;
            }

            return datalayer;
        }
    }

    @Nullable
    public DataLayer removeLayer(CoordinateKey sectionPos) {
        return this.map.remove(sectionPos);
    }

    public void setLayer(CoordinateKey sectionPos, DataLayer array) {
        this.map.put(sectionPos, array);
    }

    public void clearCache() {
        for (int i = 0; i < 2; i++) {
            this.lastSectionKeys[i] = null;
            this.lastSections[i] = null;
        }
    }

    public void disableCache() {
        this.cacheEnabled = false;
    }
}
