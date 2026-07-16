package org.verselstudios.noisecustomizer.overwrites.light;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public final class ExtendedBlockLightEngine extends ExtendedLightEngine<ExtendedBlockLightSectionStorage.ExtendedBlockDataLayerStorageMap, ExtendedBlockLightSectionStorage> {
    private final BlockPos.MutableBlockPos mutablePos;

    public ExtendedBlockLightEngine(LightChunkGetter chunkSource) {
        this(chunkSource, new ExtendedBlockLightSectionStorage(chunkSource));
    }

    @VisibleForTesting
    public ExtendedBlockLightEngine(LightChunkGetter chunkSource, ExtendedBlockLightSectionStorage storage) {
        super(chunkSource, storage);
        this.mutablePos = new BlockPos.MutableBlockPos();
    }

    protected void checkNode(CoordinateKey levelPos) {
        CoordinateKey sectionPos = levelPos.toSection();
        if ((this.storage).storingLightForSection(sectionPos)) {
            BlockState blockstate = this.getState(this.mutablePos.set(levelPos.x(), levelPos.y(), levelPos.z()));
            int j = this.getEmission(levelPos, blockstate);
            int k = (this.storage).getStoredLevel(levelPos);
            if (j < k) {
                (this.storage).setStoredLevel(levelPos, 0);
                this.enqueueDecrease(levelPos, QueueEntry.decreaseAllDirections(k));
            } else {
                this.enqueueDecrease(levelPos, PULL_LIGHT_IN_ENTRY);
            }

            if (j > 0) {
                this.enqueueIncrease(levelPos, QueueEntry.increaseLightFromEmission(j, isEmptyShape(blockstate)));
            }
        }

    }

    protected void propagateIncrease(CoordinateKey levelPos, long queueEntry, int lightLevel) {
        BlockState blockstate = null;
        Direction[] var7 = PROPAGATION_DIRECTIONS;
        int var8 = var7.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            Direction direction = var7[var9];
            if (QueueEntry.shouldPropagateInDirection(queueEntry, direction)) {
                CoordinateKey offset = levelPos.offset(direction);
                if ((this.storage).storingLightForSection(offset.toSection())) {
                    int j = (this.storage).getStoredLevel(offset);
                    int k = lightLevel - 1;
                    if (k > j) {
                        this.mutablePos.set(offset.x(), offset.y(), offset.z());
                        BlockState blockstate1 = this.getState(this.mutablePos);
                        int l = lightLevel - this.getOpacity(blockstate1, this.mutablePos);
                        if (l > j) {
                            if (blockstate == null) {
                                blockstate = QueueEntry.isFromEmptyShape(queueEntry) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(levelPos.x(), levelPos.y(), levelPos.z()));
                            }

                            if (!this.shapeOccludes(levelPos, blockstate, offset, blockstate1, direction)) {
                                (this.storage).setStoredLevel(offset, l);
                                if (l > 1) {
                                    this.enqueueIncrease(offset, QueueEntry.increaseSkipOneDirection(l, isEmptyShape(blockstate1), direction.getOpposite()));
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    protected void propagateDecrease(CoordinateKey levelPos, long lightLevel) {
        int i = QueueEntry.getFromLevel(lightLevel);
        Direction[] var6 = PROPAGATION_DIRECTIONS;
        int var7 = var6.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            Direction direction = var6[var8];
            if (QueueEntry.shouldPropagateInDirection(lightLevel, direction)) {
                CoordinateKey offset = levelPos.offset(direction);
                if ((this.storage).storingLightForSection(offset.toSection())) {
                    int k = (this.storage).getStoredLevel(offset);
                    if (k != 0) {
                        if (k <= i - 1) {
                            BlockState blockstate = this.getState(this.mutablePos.set(offset.x(), offset.y(), offset.z()));
                            int l = this.getEmission(offset, blockstate);
                            (this.storage).setStoredLevel(offset, 0);
                            if (l < k) {
                                this.enqueueDecrease(offset, QueueEntry.decreaseSkipOneDirection(k, direction.getOpposite()));
                            }

                            if (l > 0) {
                                this.enqueueIncrease(offset, QueueEntry.increaseLightFromEmission(l, isEmptyShape(blockstate)));
                            }
                        } else {
                            this.enqueueIncrease(offset, QueueEntry.increaseOnlyOneDirection(k, false, direction.getOpposite()));
                        }
                    }
                }
            }
        }

    }

    private int getEmission(CoordinateKey levelPos, BlockState state) {
        int i = state.getLightEmission(this.chunkSource.getLevel(), this.mutablePos);
        return i > 0 && (this.storage).lightOnInSection(levelPos.toSection()) ? i : 0;
    }

    public void propagateLightSources(ChunkPos chunkPos) {
        this.setLightEnabled(chunkPos, true);
        LightChunk lightchunk = this.chunkSource.getChunkForLighting(chunkPos.x, chunkPos.z);
        if (lightchunk != null) {
            lightchunk.findBlockLightSources((pos, state) -> {
                int i = state.getLightEmission(this.chunkSource.getLevel(), pos);
                this.enqueueIncrease(CoordinateKey.fromBlock(pos), QueueEntry.increaseLightFromEmission(i, isEmptyShape(state)));
            });
        }

    }
}
