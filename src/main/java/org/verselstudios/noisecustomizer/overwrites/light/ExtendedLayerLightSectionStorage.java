package org.verselstudios.noisecustomizer.overwrites.light;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.*;

import javax.annotation.Nullable;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public abstract class ExtendedLayerLightSectionStorage<M extends ExtendedDataLayerStorageMap<M>> {
    private final LightLayer layer;
    protected final LightChunkGetter chunkSource;
    protected final Object2ByteMap<CoordinateKey> sectionStates = new Object2ByteOpenHashMap<>();
    private final ObjectSet<CoordinateKey> columnsWithSources = new ObjectOpenHashSet<>();
    protected volatile M visibleSectionData;
    protected final M updatingSectionData;
    protected final ObjectSet<CoordinateKey> changedSections = new ObjectOpenHashSet<>();
    protected final ObjectSet<CoordinateKey> sectionsAffectedByLightUpdates = new ObjectOpenHashSet<>();
    protected final Object2ObjectMap<CoordinateKey, DataLayer> queuedSections = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    /**
     * Section column positions (section positions with Y=0) that need to be kept even if some of their sections could otherwise be removed.
     */
    private final ObjectSet<CoordinateKey> columnsToRetainQueuedDataFor = new ObjectOpenHashSet<>();
    /**
     * Set of section positions that can be removed, because their light won't affect any blocks.
     */
    private final ObjectSet<CoordinateKey> toRemove = new ObjectOpenHashSet<>();
    protected volatile boolean hasInconsistencies;

    protected ExtendedLayerLightSectionStorage(LightLayer layer, LightChunkGetter chunkSource, M updatingSectionData) {
        this.layer = layer;
        this.chunkSource = chunkSource;
        this.updatingSectionData = updatingSectionData;
        this.visibleSectionData = updatingSectionData.copy();
        this.visibleSectionData.disableCache();
        this.sectionStates.defaultReturnValue((byte)0);
    }

    protected boolean storingLightForSection(CoordinateKey sectionPos) {
        return this.getDataLayer(sectionPos, true) != null;
    }

    @Nullable
    protected DataLayer getDataLayer(CoordinateKey sectionPos, boolean cached) {
        return this.getDataLayer(cached ? this.updatingSectionData : this.visibleSectionData, sectionPos);
    }

    @Nullable
    protected DataLayer getDataLayer(M map, CoordinateKey sectionPos) {
        return map.getLayer(sectionPos);
    }

    @Nullable
    protected DataLayer getDataLayerToWrite(CoordinateKey sectionPos) {
        DataLayer datalayer = this.updatingSectionData.getLayer(sectionPos);
        if (datalayer == null) {
            return null;
        } else {
            if (this.changedSections.add(sectionPos)) {
                datalayer = datalayer.copy();
                this.updatingSectionData.setLayer(sectionPos, datalayer);
                this.updatingSectionData.clearCache();
            }

            return datalayer;
        }
    }

    @Nullable
    public DataLayer getDataLayerData(CoordinateKey sectionPos) {
        DataLayer datalayer = this.queuedSections.get(sectionPos);
        return datalayer != null ? datalayer : this.getDataLayer(sectionPos, false);
    }

    protected abstract int getLightValue(CoordinateKey levelPos);

    protected int getStoredLevel(CoordinateKey levelPos) {
        CoordinateKey i = levelPos.toSection();
        DataLayer datalayer = this.getDataLayer(i, true);
        CoordinateKey rel = levelPos.sectionRelative();
        return datalayer.get(
                rel.x(),
                rel.y(),
                rel.z()
        );
    }

    protected void setStoredLevel(CoordinateKey levelPos, int lightLevel) {
        CoordinateKey i = levelPos.toSection();
        DataLayer datalayer;
        if (this.changedSections.add(i)) {
            datalayer = this.updatingSectionData.copyDataLayer(i);
        } else {
            datalayer = this.getDataLayer(i, true);
        }
        CoordinateKey rel = levelPos.sectionRelative();
        datalayer.set(
                rel.x(),
                rel.y(),
                rel.z(),
                lightLevel
        );
        CoordinateKey.aroundAndAt(levelPos, this.sectionsAffectedByLightUpdates::add);
    }

    protected void markSectionAndNeighborsAsAffected(CoordinateKey sectionPos) {
        int i = sectionPos.x();
        int j = sectionPos.y();
        int k = sectionPos.z();

        for (int i1 = -1; i1 <= 1; i1++) {
            for (int j1 = -1; j1 <= 1; j1++) {
                for (int k1 = -1; k1 <= 1; k1++) {
                    this.sectionsAffectedByLightUpdates.add(sectionPos.offset(i1, j1, k1));
                }
            }
        }
    }

    protected DataLayer createDataLayer(CoordinateKey sectionPos) {
        DataLayer datalayer = this.queuedSections.get(sectionPos);
        return datalayer != null ? datalayer : new DataLayer();
    }

    protected boolean hasInconsistencies() {
        return this.hasInconsistencies;
    }

    protected void markNewInconsistencies() {
        if (this.hasInconsistencies) {
            this.hasInconsistencies = false;

            for (CoordinateKey key : this.toRemove) {
                DataLayer datalayer = this.queuedSections.remove(key);
                DataLayer datalayer1 = this.updatingSectionData.removeLayer(key);
                if (this.columnsToRetainQueuedDataFor.contains(key.getZeroNode())) {
                    if (datalayer != null) {
                        this.queuedSections.put(key, datalayer);
                    } else if (datalayer1 != null) {
                        this.queuedSections.put(key, datalayer1);
                    }
                }
            }

            this.updatingSectionData.clearCache();

            for (CoordinateKey key : this.toRemove) {
                this.onNodeRemoved(key);
                this.changedSections.add(key);
            }

            this.toRemove.clear();
            ObjectIterator<Entry<CoordinateKey, DataLayer>> objectiterator = Object2ObjectMaps.fastIterator(this.queuedSections);

            while (objectiterator.hasNext()) {
                Entry<CoordinateKey, DataLayer> entry = objectiterator.next();
                CoordinateKey key = entry.getKey();
                if (this.storingLightForSection(key)) {
                    DataLayer datalayer2 = entry.getValue();
                    if (this.updatingSectionData.getLayer(key) != datalayer2) {
                        this.updatingSectionData.setLayer(key, datalayer2);
                        this.changedSections.add(key);
                    }

                    objectiterator.remove();
                }
            }

            this.updatingSectionData.clearCache();
        }
    }

    protected void onNodeAdded(CoordinateKey sectionPos) {
    }

    protected void onNodeRemoved(CoordinateKey sectionPos) {
    }

    protected void setLightEnabled(CoordinateKey sectionPos, boolean lightEnabled) {
        if (lightEnabled) {
            this.columnsWithSources.add(sectionPos);
        } else {
            this.columnsWithSources.remove(sectionPos);
        }
    }

    protected boolean lightOnInSection(CoordinateKey sectionPos) {
        CoordinateKey column = sectionPos.toColumn();
        return this.columnsWithSources.contains(column);
    }

    public void retainData(CoordinateKey sectionColumnPos, boolean retain) {
        if (retain) {
            this.columnsToRetainQueuedDataFor.add(sectionColumnPos);
        } else {
            this.columnsToRetainQueuedDataFor.remove(sectionColumnPos);
        }
    }

    protected void queueSectionData(CoordinateKey sectionPos, @Nullable DataLayer data) {
        if (data != null) {
            this.queuedSections.put(sectionPos, data);
            this.hasInconsistencies = true;
        } else {
            this.queuedSections.remove(sectionPos);
        }
    }

    protected void updateSectionStatus(CoordinateKey sectionPos, boolean isEmpty) {
        byte b0 = this.sectionStates.getByte(sectionPos);
        byte b1 = ExtendedLayerLightSectionStorage.SectionState.hasData(b0, !isEmpty);
        if (b0 != b1) {
            this.putSectionState(sectionPos, b1);
            int i = isEmpty ? -1 : 1;

            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    for (int l = -1; l <= 1; l++) {
                        if (j != 0 || k != 0 || l != 0) {
                            CoordinateKey i1 = sectionPos.offset(j,k,l);
                            byte b2 = this.sectionStates.getByte(i1);
                            this.putSectionState(
                                    i1, ExtendedLayerLightSectionStorage.SectionState.neighborCount(b2, ExtendedLayerLightSectionStorage.SectionState.neighborCount(b2) + i)
                            );
                        }
                    }
                }
            }
        }
    }

    protected void putSectionState(CoordinateKey sectionPos, byte sectionState) {
        if (sectionState != 0) {
            if (this.sectionStates.put(sectionPos, sectionState) == 0) {
                this.initializeSection(sectionPos);
            }
        } else if (this.sectionStates.removeByte(sectionPos) != 0) {
            this.removeSection(sectionPos);
        }
    }

    private void initializeSection(CoordinateKey sectionPos) {
        if (!this.toRemove.remove(sectionPos)) {
            this.updatingSectionData.setLayer(sectionPos, this.createDataLayer(sectionPos));
            this.changedSections.add(sectionPos);
            this.onNodeAdded(sectionPos);
            this.markSectionAndNeighborsAsAffected(sectionPos);
            this.hasInconsistencies = true;
        }
    }

    private void removeSection(CoordinateKey sectionPos) {
        this.toRemove.add(sectionPos);
        this.hasInconsistencies = true;
    }

    protected void swapSectionMap() {
        if (!this.changedSections.isEmpty()) {
            M m = this.updatingSectionData.copy();
            m.disableCache();
            this.visibleSectionData = m;
            this.changedSections.clear();
        }

        if (!this.sectionsAffectedByLightUpdates.isEmpty()) {
            ObjectIterator<CoordinateKey> objectIterator = this.sectionsAffectedByLightUpdates.iterator();

            while (objectIterator.hasNext()) {
                CoordinateKey next = objectIterator.next();
                this.chunkSource.onLightUpdate(this.layer, SectionPos.of(next.x(), next.y(), next.z()));
            }

            this.sectionsAffectedByLightUpdates.clear();
        }
    }

    public ExtendedLayerLightSectionStorage.SectionType getDebugSectionType(CoordinateKey sectionPos) {
        return ExtendedLayerLightSectionStorage.SectionState.type(this.sectionStates.get(sectionPos));
    }

    protected static class SectionState {
        public static final byte EMPTY = 0;
        private static final int MIN_NEIGHBORS = 0;
        private static final int MAX_NEIGHBORS = 26;
        private static final byte HAS_DATA_BIT = 32;
        private static final byte NEIGHBOR_COUNT_BITS = 31;

        public static byte hasData(byte sectionState, boolean hasData) {
            return (byte)(hasData ? sectionState | 32 : sectionState & -33);
        }

        public static byte neighborCount(byte sectionState, int neighborCount) {
            if (neighborCount >= 0 && neighborCount <= 26) {
                return (byte)(sectionState & -32 | neighborCount & 31);
            } else {
                throw new IllegalArgumentException("Neighbor count was not within range [0; 26]");
            }
        }

        public static boolean hasData(byte sectionState) {
            return (sectionState & 32) != 0;
        }

        public static int neighborCount(byte sectionState) {
            return sectionState & 31;
        }

        public static ExtendedLayerLightSectionStorage.SectionType type(byte sectionState) {
            if (sectionState == 0) {
                return ExtendedLayerLightSectionStorage.SectionType.EMPTY;
            } else {
                return hasData(sectionState) ? ExtendedLayerLightSectionStorage.SectionType.LIGHT_AND_DATA : ExtendedLayerLightSectionStorage.SectionType.LIGHT_ONLY;
            }
        }
    }

    public static enum SectionType {
        EMPTY("2"),
        LIGHT_ONLY("1"),
        LIGHT_AND_DATA("0");

        private final String display;

        SectionType(String display) {
            this.display = display;
        }

        public String display() {
            return this.display;
        }

        public LayerLightSectionStorage.SectionType convert() {
            return switch (this) {
                case LIGHT_ONLY -> LayerLightSectionStorage.SectionType.LIGHT_ONLY;
                case LIGHT_AND_DATA -> LayerLightSectionStorage.SectionType.LIGHT_AND_DATA;
                default -> LayerLightSectionStorage.SectionType.EMPTY;
            };
        }
    }
}
