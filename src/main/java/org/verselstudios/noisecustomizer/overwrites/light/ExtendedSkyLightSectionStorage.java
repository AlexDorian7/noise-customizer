package org.verselstudios.noisecustomizer.overwrites.light;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public class ExtendedSkyLightSectionStorage extends ExtendedLayerLightSectionStorage<ExtendedSkyLightSectionStorage.ExtendedSkyDataLayerStorageMap> {
    protected ExtendedSkyLightSectionStorage(LightChunkGetter chunkSource) {
        super(
                LightLayer.SKY,
                chunkSource,
                new ExtendedSkyDataLayerStorageMap(new Object2ObjectOpenHashMap<>(), new Object2IntOpenHashMap<>(), Integer.MAX_VALUE)
        );
    }

    @Override
    protected int getLightValue(CoordinateKey levelPos) {
        return this.getLightValue(levelPos, false);
    }

    protected int getLightValue(CoordinateKey levelPos, boolean updateAll) {
        CoordinateKey sectionPos = levelPos.toSection();
        int sy = sectionPos.y();
        ExtendedSkyDataLayerStorageMap storageMap = updateAll
                ? this.updatingSectionData
                : this.visibleSectionData;
        int k = storageMap.topSections.get(sectionPos.getZeroNode());
        if (k != storageMap.currentLowestY && sy < k) {
            DataLayer datalayer = this.getDataLayer(storageMap, sectionPos);
            if (datalayer == null) {
                for (levelPos = levelPos.getFlatIndex();
                     datalayer == null;
                     datalayer = this.getDataLayer(storageMap, sectionPos)
                ) {
                    if (++sy >= k) {
                        return 15;
                    }

                    sectionPos = sectionPos.offset(Direction.UP);
                }
            }

            CoordinateKey rel = levelPos.sectionRelative();
            return datalayer.get(
                    rel.x(),
                    rel.y(),
                    rel.z()
            );
        } else {
            return updateAll && !this.lightOnInSection(sectionPos) ? 0 : 15;
        }
    }

    @Override
    protected void onNodeAdded(CoordinateKey sectionPos) {
        int y = sectionPos.y();
        if (this.updatingSectionData.currentLowestY > y) {
            this.updatingSectionData.currentLowestY = y;
            this.updatingSectionData.topSections.defaultReturnValue(this.updatingSectionData.currentLowestY);
        }

        CoordinateKey zeroNode = sectionPos.getZeroNode();
        int k = this.updatingSectionData.topSections.getInt(zeroNode);
        if (k < y + 1) {
            this.updatingSectionData.topSections.put(zeroNode, y + 1);
        }
    }

    @Override
    protected void onNodeRemoved(CoordinateKey sectionPos) {
        CoordinateKey zeroNode = sectionPos.getZeroNode();
        int y = sectionPos.y();
        if (this.updatingSectionData.topSections.getInt(zeroNode) == y + 1) {
            CoordinateKey down;
            for (down = sectionPos; !this.storingLightForSection(down) && this.hasLightDataAtOrBelow(y); down = down.offset(Direction.DOWN)) {
                y--;
            }

            if (this.storingLightForSection(down)) {
                this.updatingSectionData.topSections.put(zeroNode, y + 1);
            } else {
                this.updatingSectionData.topSections.removeInt(zeroNode);
            }
        }
    }

    @Override
    protected DataLayer createDataLayer(CoordinateKey sectionPos) {
        DataLayer datalayer = this.queuedSections.get(sectionPos);
        if (datalayer != null) {
            return datalayer;
        } else {
            int i = this.updatingSectionData.topSections.getInt(sectionPos.getZeroNode());
            if (i != this.updatingSectionData.currentLowestY && sectionPos.y() < i) {
                CoordinateKey up = sectionPos.offset(Direction.UP);

                DataLayer datalayer1;
                while ((datalayer1 = this.getDataLayer(up, true)) == null) {
                    up = up.offset(Direction.UP);
                }

                return repeatFirstLayer(datalayer1);
            } else {
                return this.lightOnInSection(sectionPos) ? new DataLayer(15) : new DataLayer();
            }
        }
    }

    private static DataLayer repeatFirstLayer(DataLayer dataLayer) {
        if (dataLayer.isDefinitelyHomogenous()) {
            return dataLayer.copy();
        } else {
            byte[] abyte = dataLayer.getData();
            byte[] abyte1 = new byte[2048];

            for (int i = 0; i < 16; i++) {
                System.arraycopy(abyte, 0, abyte1, i * 128, 128);
            }

            return new DataLayer(abyte1);
        }
    }

    protected boolean hasLightDataAtOrBelow(int y) {
        return y >= this.updatingSectionData.currentLowestY;
    }

    protected boolean isAboveData(CoordinateKey sectionPos) {
        CoordinateKey zeroNode = sectionPos.getZeroNode();
        int j = this.updatingSectionData.topSections.getInt(zeroNode);
        return j == this.updatingSectionData.currentLowestY || sectionPos.y() >= j;
    }

    protected int getTopSectionY(CoordinateKey sectionPos) {
        return this.updatingSectionData.topSections.getInt(sectionPos);
    }

    protected int getBottomSectionY() {
        return this.updatingSectionData.currentLowestY;
    }

    protected static final class ExtendedSkyDataLayerStorageMap extends ExtendedDataLayerStorageMap<ExtendedSkyDataLayerStorageMap> {
        int currentLowestY;
        final Object2IntOpenHashMap<CoordinateKey> topSections;

        public ExtendedSkyDataLayerStorageMap(Object2ObjectOpenHashMap<CoordinateKey, DataLayer> map, Object2IntOpenHashMap<CoordinateKey> topSections, int currentLowestY) {
            super(map);
            this.topSections = topSections;
            topSections.defaultReturnValue(currentLowestY);
            this.currentLowestY = currentLowestY;
        }

        public ExtendedSkyDataLayerStorageMap copy() {
            return new ExtendedSkyDataLayerStorageMap(this.map.clone(), this.topSections.clone(), this.currentLowestY);
        }
    }
}
