package org.verselstudios.noisecustomizer.overwrites.light;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import org.jetbrains.annotations.VisibleForTesting;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public final class ExtendedSkyLightEngine extends ExtendedLightEngine<ExtendedSkyLightSectionStorage.ExtendedSkyDataLayerStorageMap, ExtendedSkyLightSectionStorage> {
    private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = ExtendedLightEngine.QueueEntry.decreaseAllDirections(15);
    private static final long REMOVE_SKY_SOURCE_ENTRY = ExtendedLightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
    private static final long ADD_SKY_SOURCE_ENTRY = ExtendedLightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private final ChunkSkyLightSources emptyChunkSources;

    public ExtendedSkyLightEngine(LightChunkGetter chunkSource) {
        this(chunkSource, new ExtendedSkyLightSectionStorage(chunkSource));
    }

    @VisibleForTesting
    protected ExtendedSkyLightEngine(LightChunkGetter chunkSource, ExtendedSkyLightSectionStorage sectionStorage) {
        super(chunkSource, sectionStorage);
        this.emptyChunkSources = new ChunkSkyLightSources(chunkSource.getLevel());
    }

    private static boolean isSourceLevel(int level) {
        return level == 15;
    }

    private int getLowestSourceY(int x, int z, int defaultReturnValue) {
        ChunkSkyLightSources chunkskylightsources = this.getChunkSources(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        return chunkskylightsources == null
                ? defaultReturnValue
                : chunkskylightsources.getLowestSourceY(SectionPos.sectionRelative(x), SectionPos.sectionRelative(z));
    }

    @Nullable
    private ChunkSkyLightSources getChunkSources(int chunkX, int chunkZ) {
        LightChunk lightchunk = this.chunkSource.getChunkForLighting(chunkX, chunkZ);
        return lightchunk != null ? lightchunk.getSkyLightSources() : null;
    }

    @Override
    protected void checkNode(CoordinateKey levelPos) {
        int i = levelPos.x();
        int j = levelPos.y();
        int k = levelPos.z();
        CoordinateKey sectionPos = levelPos.toSection();
        int i1 = this.storage.lightOnInSection(sectionPos) ? this.getLowestSourceY(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (i1 != Integer.MAX_VALUE) {
            this.updateSourcesInColumn(i, k, i1);
        }

        if (this.storage.storingLightForSection(sectionPos)) {
            boolean flag = j >= i1;
            if (flag) {
                this.enqueueDecrease(levelPos, REMOVE_SKY_SOURCE_ENTRY);
                this.enqueueIncrease(levelPos, ADD_SKY_SOURCE_ENTRY);
            } else {
                int j1 = this.storage.getStoredLevel(levelPos);
                if (j1 > 0) {
                    this.storage.setStoredLevel(levelPos, 0);
                    this.enqueueDecrease(levelPos, ExtendedLightEngine.QueueEntry.decreaseAllDirections(j1));
                } else {
                    this.enqueueDecrease(levelPos, PULL_LIGHT_IN_ENTRY);
                }
            }
        }
    }

    private void updateSourcesInColumn(int x, int z, int lowestY) {
        int i = SectionPos.sectionToBlockCoord(this.storage.getBottomSectionY());
        this.removeSourcesBelow(x, z, lowestY, i);
        this.addSourcesAbove(x, z, lowestY, i);
    }

    private void removeSourcesBelow(int x, int z, int minY, int bottomSectionY) {
        if (minY > bottomSectionY) {
            int i = SectionPos.blockToSectionCoord(x);
            int j = SectionPos.blockToSectionCoord(z);
            int k = minY - 1;

            for (int l = SectionPos.blockToSectionCoord(k); this.storage.hasLightDataAtOrBelow(l); l--) {
                if (this.storage.storingLightForSection(new CoordinateKey(i, l, j))) {
                    int i1 = SectionPos.sectionToBlockCoord(l);
                    int j1 = i1 + 15;

                    for (int k1 = Math.min(j1, k); k1 >= i1; k1--) {
                        CoordinateKey blockPos = new CoordinateKey(x, k1, z);
                        if (!isSourceLevel(this.storage.getStoredLevel(blockPos))) {
                            return;
                        }

                        this.storage.setStoredLevel(blockPos, 0);
                        this.enqueueDecrease(blockPos, k1 == minY - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
                    }
                }
            }
        }
    }

    private void addSourcesAbove(int x, int z, int maxY, int bottomSectionY) {
        int i = SectionPos.blockToSectionCoord(x);
        int j = SectionPos.blockToSectionCoord(z);
        int k = Math.max(
                Math.max(this.getLowestSourceY(x - 1, z, Integer.MIN_VALUE), this.getLowestSourceY(x + 1, z, Integer.MIN_VALUE)),
                Math.max(this.getLowestSourceY(x, z - 1, Integer.MIN_VALUE), this.getLowestSourceY(x, z + 1, Integer.MIN_VALUE))
        );
        int l = Math.max(maxY, bottomSectionY);

        for (CoordinateKey sectionPos = new CoordinateKey(i, SectionPos.blockToSectionCoord(l), j); !this.storage.isAboveData(sectionPos); sectionPos = sectionPos.offset(Direction.UP)) {
            if (this.storage.storingLightForSection(sectionPos)) {
                int j1 = SectionPos.sectionToBlockCoord(sectionPos.y());
                int k1 = j1 + 15;

                for (int l1 = Math.max(j1, l); l1 <= k1; l1++) {
                    CoordinateKey blockPos = new CoordinateKey(x, l1, z);
                    if (isSourceLevel(this.storage.getStoredLevel(blockPos))) {
                        return;
                    }

                    this.storage.setStoredLevel(blockPos, 15);
                    if (l1 < k || l1 == maxY) {
                        this.enqueueIncrease(blockPos, ADD_SKY_SOURCE_ENTRY);
                    }
                }
            }
        }
    }

    @Override
    protected void propagateIncrease(CoordinateKey levelPos, long queueEntry, int lightLevel) {
        BlockState blockstate = null;
        int i = this.countEmptySectionsBelowIfAtBorder(levelPos);

        for (Direction direction : PROPAGATION_DIRECTIONS) {
            if (ExtendedLightEngine.QueueEntry.shouldPropagateInDirection(queueEntry, direction)) {
                CoordinateKey offset = levelPos.offset(direction);
                if (this.storage.storingLightForSection(offset.toSection())) {
                    int k = this.storage.getStoredLevel(offset);
                    int l = lightLevel - 1;
                    if (l > k) {
                        this.mutablePos.set(offset.x(), offset.y(), offset.z());
                        BlockState blockstate1 = this.getState(this.mutablePos);
                        int i1 = lightLevel - this.getOpacity(blockstate1, this.mutablePos);
                        if (i1 > k) {
                            if (blockstate == null) {
                                blockstate = ExtendedLightEngine.QueueEntry.isFromEmptyShape(queueEntry)
                                        ? Blocks.AIR.defaultBlockState()
                                        : this.getState(this.mutablePos.set(levelPos.x(), levelPos.y(), levelPos.z()));
                            }

                            if (!this.shapeOccludes(levelPos, blockstate, offset, blockstate1, direction)) {
                                this.storage.setStoredLevel(offset, i1);
                                if (i1 > 1) {
                                    this.enqueueIncrease(
                                            offset, ExtendedLightEngine.QueueEntry.increaseSkipOneDirection(i1, isEmptyShape(blockstate1), direction.getOpposite())
                                    );
                                }

                                this.propagateFromEmptySections(offset, direction, i1, true, i);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void propagateDecrease(CoordinateKey levelPos, long lightLevel) {
        int i = this.countEmptySectionsBelowIfAtBorder(levelPos);
        int j = ExtendedLightEngine.QueueEntry.getFromLevel(lightLevel);

        for (Direction direction : PROPAGATION_DIRECTIONS) {
            if (ExtendedLightEngine.QueueEntry.shouldPropagateInDirection(lightLevel, direction)) {
                CoordinateKey offset = levelPos.offset(direction);
                if (this.storage.storingLightForSection(offset.toSection())) {
                    int l = this.storage.getStoredLevel(offset);
                    if (l != 0) {
                        if (l <= j - 1) {
                            this.storage.setStoredLevel(offset, 0);
                            this.enqueueDecrease(offset, ExtendedLightEngine.QueueEntry.decreaseSkipOneDirection(l, direction.getOpposite()));
                            this.propagateFromEmptySections(offset, direction, l, false, i);
                        } else {
                            this.enqueueIncrease(offset, ExtendedLightEngine.QueueEntry.increaseOnlyOneDirection(l, false, direction.getOpposite()));
                        }
                    }
                }
            }
        }
    }

    private int countEmptySectionsBelowIfAtBorder(CoordinateKey levelPos) {
        int y = levelPos.y();
        int j = SectionPos.sectionRelative(y);
        if (j != 0) {
            return 0;
        } else {
            int x = levelPos.x();
            int z = levelPos.z();
            int i1 = SectionPos.sectionRelative(x);
            int j1 = SectionPos.sectionRelative(z);
            if (i1 != 0 && i1 != 15 && j1 != 0 && j1 != 15) {
                return 0;
            } else {
                int k1 = SectionPos.blockToSectionCoord(x);
                int l1 = SectionPos.blockToSectionCoord(y);
                int i2 = SectionPos.blockToSectionCoord(z);
                int j2 = 0;

                while (!this.storage.storingLightForSection(new CoordinateKey(k1, l1 - j2 - 1, i2)) && this.storage.hasLightDataAtOrBelow(l1 - j2 - 1)) {
                    j2++;
                }

                return j2;
            }
        }
    }

    private void propagateFromEmptySections(CoordinateKey levelPos, Direction direction, int level, boolean shouldIncrease, int emptySections) {
        if (emptySections != 0) {
            int x = levelPos.x();
            int z = levelPos.z();
            if (crossedSectionEdge(direction, SectionPos.sectionRelative(x), SectionPos.sectionRelative(z))) {
                int y = levelPos.y();
                int sx = SectionPos.blockToSectionCoord(x);
                int sz = SectionPos.blockToSectionCoord(z);
                int sy = SectionPos.blockToSectionCoord(y) - 1;
                int k1 = sy - emptySections + 1;

                while (sy >= k1) {
                    if (!this.storage.storingLightForSection(new CoordinateKey(sx, sy, sz))) {
                        sy--;
                    } else {
                        int l1 = SectionPos.sectionToBlockCoord(sy);

                        for (int i2 = 15; i2 >= 0; i2--) {
                            CoordinateKey blockPos = new CoordinateKey(x, l1 + i2, z);
                            if (shouldIncrease) {
                                this.storage.setStoredLevel(blockPos, level);
                                if (level > 1) {
                                    this.enqueueIncrease(blockPos, ExtendedLightEngine.QueueEntry.increaseSkipOneDirection(level, true, direction.getOpposite()));
                                }
                            } else {
                                this.storage.setStoredLevel(blockPos, 0);
                                this.enqueueDecrease(blockPos, ExtendedLightEngine.QueueEntry.decreaseSkipOneDirection(level, direction.getOpposite()));
                            }
                        }

                        sy--;
                    }
                }
            }
        }
    }

    private static boolean crossedSectionEdge(Direction direction, int x, int z) {
        return switch (direction) {
            case NORTH -> z == 15;
            case SOUTH -> z == 0;
            case WEST -> x == 15;
            case EAST -> x == 0;
            default -> false;
        };
    }

    @Override
    public void setLightEnabled(ChunkPos chunkPos, boolean lightEnabled) {
        super.setLightEnabled(chunkPos, lightEnabled);
        if (lightEnabled) {
            ChunkSkyLightSources chunkskylightsources = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x, chunkPos.z), this.emptyChunkSources);
            int i = chunkskylightsources.getHighestLowestSourceY() - 1;
            int j = SectionPos.blockToSectionCoord(i) + 1;
            CoordinateKey k = CoordinateKey.fromChunk(chunkPos);
            int l = this.storage.getTopSectionY(k);
            int i1 = Math.max(this.storage.getBottomSectionY(), j);

            for (int j1 = l - 1; j1 >= i1; j1--) {
                DataLayer datalayer = this.storage.getDataLayerToWrite(new CoordinateKey(chunkPos.x, j1, chunkPos.z));
                if (datalayer != null && datalayer.isEmpty()) {
                    datalayer.fill(15);
                }
            }
        }
    }

    @Override
    public void propagateLightSources(ChunkPos chunkPos) {
        CoordinateKey i = CoordinateKey.fromChunk(chunkPos);
        this.storage.setLightEnabled(i, true);
        ChunkSkyLightSources chunkskylightsources = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x, chunkPos.z), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources1 = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x, chunkPos.z - 1), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources2 = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x, chunkPos.z + 1), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources3 = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x - 1, chunkPos.z), this.emptyChunkSources);
        ChunkSkyLightSources chunkskylightsources4 = Objects.requireNonNullElse(this.getChunkSources(chunkPos.x + 1, chunkPos.z), this.emptyChunkSources);
        int j = this.storage.getTopSectionY(i);
        int k = this.storage.getBottomSectionY();
        int l = SectionPos.sectionToBlockCoord(chunkPos.x);
        int i1 = SectionPos.sectionToBlockCoord(chunkPos.z);

        for (int j1 = j - 1; j1 >= k; j1--) {
            CoordinateKey k1 = new CoordinateKey(chunkPos.x, j1, chunkPos.z);
            DataLayer datalayer = this.storage.getDataLayerToWrite(k1);
            if (datalayer != null) {
                int l1 = SectionPos.sectionToBlockCoord(j1);
                int i2 = l1 + 15;
                boolean flag = false;

                for (int j2 = 0; j2 < 16; j2++) {
                    for (int k2 = 0; k2 < 16; k2++) {
                        int l2 = chunkskylightsources.getLowestSourceY(k2, j2);
                        if (l2 <= i2) {
                            int i3 = j2 == 0 ? chunkskylightsources1.getLowestSourceY(k2, 15) : chunkskylightsources.getLowestSourceY(k2, j2 - 1);
                            int j3 = j2 == 15 ? chunkskylightsources2.getLowestSourceY(k2, 0) : chunkskylightsources.getLowestSourceY(k2, j2 + 1);
                            int k3 = k2 == 0 ? chunkskylightsources3.getLowestSourceY(15, j2) : chunkskylightsources.getLowestSourceY(k2 - 1, j2);
                            int l3 = k2 == 15 ? chunkskylightsources4.getLowestSourceY(0, j2) : chunkskylightsources.getLowestSourceY(k2 + 1, j2);
                            int i4 = Math.max(Math.max(i3, j3), Math.max(k3, l3));

                            for (int j4 = i2; j4 >= Math.max(l1, l2); j4--) {
                                datalayer.set(k2, SectionPos.sectionRelative(j4), j2, 15);
                                if (j4 == l2 || j4 < i4) {
                                    CoordinateKey k4 = new CoordinateKey(l + k2, j4, i1 + j2);
                                    this.enqueueIncrease(k4, ExtendedLightEngine.QueueEntry.increaseSkySourceInDirections(j4 == l2, j4 < i3, j4 < j3, j4 < k3, j4 < l3));
                                }
                            }

                            if (l2 < l1) {
                                flag = true;
                            }
                        }
                    }
                }

                if (!flag) {
                    break;
                }
            }
        }
    }
}
