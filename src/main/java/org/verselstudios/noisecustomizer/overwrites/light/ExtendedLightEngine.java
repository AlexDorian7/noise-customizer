package org.verselstudios.noisecustomizer.overwrites.light;

import java.util.Arrays;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public abstract class ExtendedLightEngine<M extends ExtendedDataLayerStorageMap<M>, S extends ExtendedLayerLightSectionStorage<M>> implements LayerLightEventListener {
    public static final int MAX_LEVEL = 15;
    protected static final int MIN_OPACITY = 1;
    protected static final long PULL_LIGHT_IN_ENTRY = ExtendedLightEngine.QueueEntry.decreaseAllDirections(1);
    private static final int MIN_QUEUE_SIZE = 512;
    protected static final Direction[] PROPAGATION_DIRECTIONS = Direction.values();
    protected final LightChunkGetter chunkSource;
    protected final S storage;
    private final ObjectOpenHashSet<CoordinateKey> blockNodesToCheck = new ObjectOpenHashSet<>(512, 0.5F);
    private final ObjectArrayFIFOQueue<LightUpdate> decreaseQueue = new ObjectArrayFIFOQueue<>();
    private final ObjectArrayFIFOQueue<LightUpdate> increaseQueue = new ObjectArrayFIFOQueue<>();
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private static final int CACHE_SIZE = 2;
    private final long[] lastChunkPos = new long[2];
    private final LightChunk[] lastChunk = new LightChunk[2];

    protected ExtendedLightEngine(LightChunkGetter chunkSource, S storage) {
        this.chunkSource = chunkSource;
        this.storage = storage;
        this.clearChunkCache();
    }

    public static boolean hasDifferentLightProperties(BlockGetter level, BlockPos pos, BlockState state1, BlockState state2) {
        return state2 == state1
                ? false
                : state2.getLightBlock(level, pos) != state1.getLightBlock(level, pos)
                || state2.getLightEmission(level, pos) != state1.getLightEmission(level, pos)
                || state2.useShapeForLightOcclusion()
                || state1.useShapeForLightOcclusion();
    }

    public static int getLightBlockInto(
            BlockGetter level, BlockState state1, BlockPos pos1, BlockState state2, BlockPos pos2, Direction direction, int defaultReturnValue
    ) {
        boolean flag = isEmptyShape(state1);
        boolean flag1 = isEmptyShape(state2);
        if (flag && flag1) {
            return defaultReturnValue;
        } else {
            VoxelShape voxelshape = flag ? Shapes.empty() : state1.getOcclusionShape(level, pos1);
            VoxelShape voxelshape1 = flag1 ? Shapes.empty() : state2.getOcclusionShape(level, pos2);
            return Shapes.mergedFaceOccludes(voxelshape, voxelshape1, direction) ? 16 : defaultReturnValue;
        }
    }

    public static VoxelShape getOcclusionShape(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        return isEmptyShape(state) ? Shapes.empty() : state.getFaceOcclusionShape(level, pos, direction);
    }

    protected static boolean isEmptyShape(BlockState state) {
        return !state.canOcclude() || !state.useShapeForLightOcclusion();
    }

    protected BlockState getState(BlockPos pos) {
        int i = SectionPos.blockToSectionCoord(pos.getX());
        int j = SectionPos.blockToSectionCoord(pos.getZ());
        LightChunk lightchunk = this.getChunk(i, j);
        return lightchunk == null ? Blocks.BEDROCK.defaultBlockState() : lightchunk.getBlockState(pos);
    }

    protected int getOpacity(BlockState state, BlockPos pos) {
        return Math.max(1, state.getLightBlock(this.chunkSource.getLevel(), pos));
    }

    protected boolean shapeOccludes(CoordinateKey packedPos1, BlockState state1, CoordinateKey packedPos2, BlockState state2, Direction direction) {
        VoxelShape voxelshape = this.getOcclusionShape(state1, packedPos1, direction);
        VoxelShape voxelshape1 = this.getOcclusionShape(state2, packedPos2, direction.getOpposite());
        return Shapes.faceShapeOccludes(voxelshape, voxelshape1);
    }

    protected VoxelShape getOcclusionShape(BlockState state, CoordinateKey pos, Direction direction) {
        return getOcclusionShape(this.chunkSource.getLevel(), this.mutablePos.set(pos.x(), pos.y(), pos.z()), state, direction);
    }

    @Nullable
    protected LightChunk getChunk(int x, int z) {
        long i = ChunkPos.asLong(x, z);

        for (int j = 0; j < 2; j++) {
            if (i == this.lastChunkPos[j]) {
                return this.lastChunk[j];
            }
        }

        LightChunk lightchunk = this.chunkSource.getChunkForLighting(x, z);

        for (int k = 1; k > 0; k--) {
            this.lastChunkPos[k] = this.lastChunkPos[k - 1];
            this.lastChunk[k] = this.lastChunk[k - 1];
        }

        this.lastChunkPos[0] = i;
        this.lastChunk[0] = lightchunk;
        return lightchunk;
    }

    private void clearChunkCache() {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunk, null);
    }

    @Override
    public void checkBlock(BlockPos pos) {
        this.blockNodesToCheck.add(CoordinateKey.fromBlock(pos));
    }

    public void queueSectionData(CoordinateKey sectionPos, @Nullable DataLayer data) {
        this.storage.queueSectionData(sectionPos, data);
    }

    public void retainData(ChunkPos chunkPos, boolean retainData) {
        this.storage.retainData(CoordinateKey.fromChunk(chunkPos), retainData);
    }

    @Override
    public void updateSectionStatus(SectionPos pos, boolean isQueueEmpty) {
        this.storage.updateSectionStatus(CoordinateKey.fromSection(pos), isQueueEmpty);
    }

    @Override
    public void setLightEnabled(ChunkPos chunkPos, boolean lightEnabled) {
        this.storage.setLightEnabled(CoordinateKey.fromChunk(chunkPos), lightEnabled);
    }

    @Override
    public int runLightUpdates() {
        ObjectIterator<CoordinateKey> objectIterator = this.blockNodesToCheck.iterator();

        while (objectIterator.hasNext()) {
            this.checkNode(objectIterator.next());
        }

        this.blockNodesToCheck.clear();
        this.blockNodesToCheck.trim(512);
        int i = 0;
        i += this.propagateDecreases();
        i += this.propagateIncreases();
        this.clearChunkCache();
        this.storage.markNewInconsistencies();
        this.storage.swapSectionMap();
        return i;
    }

    private int propagateIncreases() {
        int i;
        for (i = 0; !this.increaseQueue.isEmpty(); i++) {
            LightUpdate update = this.increaseQueue.dequeue();
            CoordinateKey j = update.pos;
            long k = update.entry;
            int l = this.storage.getStoredLevel(j);
            int i1 = ExtendedLightEngine.QueueEntry.getFromLevel(k);
            if (ExtendedLightEngine.QueueEntry.isIncreaseFromEmission(k) && l < i1) {
                this.storage.setStoredLevel(j, i1);
                l = i1;
            }

            if (l == i1) {
                this.propagateIncrease(j, k, l);
            }
        }

        return i;
    }

    private int propagateDecreases() {
        int i;
        for (i = 0; !this.decreaseQueue.isEmpty(); i++) {
            LightUpdate update = this.decreaseQueue.dequeue();
            this.propagateDecrease(update.pos, update.entry);
        }

        return i;
    }

    protected void enqueueDecrease(CoordinateKey pos, long entry) {
        this.decreaseQueue.enqueue(new LightUpdate(pos, entry));
    }

    protected void enqueueIncrease(CoordinateKey pos, long entry) {
        this.increaseQueue.enqueue(new LightUpdate(pos, entry));
    }

    @Override
    public boolean hasLightWork() {
        return this.storage.hasInconsistencies() || !this.blockNodesToCheck.isEmpty() || !this.decreaseQueue.isEmpty() || !this.increaseQueue.isEmpty();
    }

    @Nullable
    @Override
    public DataLayer getDataLayerData(SectionPos sectionPos) {
        return this.storage.getDataLayerData(CoordinateKey.fromSection(sectionPos));
    }

    @Override
    public int getLightValue(BlockPos levelPos) {
        return this.storage.getLightValue(CoordinateKey.fromBlock(levelPos));
    }

    public String getDebugData(CoordinateKey sectionPos) {
        return this.getDebugSectionType(sectionPos).display();
    }

    public ExtendedLayerLightSectionStorage.SectionType getDebugSectionType(CoordinateKey sectionPos) {
        return this.storage.getDebugSectionType(sectionPos);
    }

    protected abstract void checkNode(CoordinateKey packedPos);

    protected abstract void propagateIncrease(CoordinateKey packedPos, long queueEntry, int lightLevel);

    protected abstract void propagateDecrease(CoordinateKey packedPos, long lightLevel);

    public static class QueueEntry {
        private static final int FROM_LEVEL_BITS = 4;
        private static final int DIRECTION_BITS = 6;
        private static final long LEVEL_MASK = 15L;
        private static final long DIRECTIONS_MASK = 1008L;
        private static final long FLAG_FROM_EMPTY_SHAPE = 1024L;
        private static final long FLAG_INCREASE_FROM_EMISSION = 2048L;

        public static long decreaseSkipOneDirection(int level, Direction direction) {
            long i = withoutDirection(1008L, direction);
            return withLevel(i, level);
        }

        public static long decreaseAllDirections(int level) {
            return withLevel(1008L, level);
        }

        public static long increaseLightFromEmission(int level, boolean fromEmptyShape) {
            long i = 1008L;
            i |= 2048L;
            if (fromEmptyShape) {
                i |= 1024L;
            }

            return withLevel(i, level);
        }

        public static long increaseSkipOneDirection(int level, boolean fromEmptyShape, Direction direction) {
            long i = withoutDirection(1008L, direction);
            if (fromEmptyShape) {
                i |= 1024L;
            }

            return withLevel(i, level);
        }

        public static long increaseOnlyOneDirection(int level, boolean fromEmptyShape, Direction direction) {
            long i = 0L;
            if (fromEmptyShape) {
                i |= 1024L;
            }

            i = withDirection(i, direction);
            return withLevel(i, level);
        }

        public static long increaseSkySourceInDirections(boolean down, boolean north, boolean south, boolean west, boolean east) {
            long i = withLevel(0L, 15);
            if (down) {
                i = withDirection(i, Direction.DOWN);
            }

            if (north) {
                i = withDirection(i, Direction.NORTH);
            }

            if (south) {
                i = withDirection(i, Direction.SOUTH);
            }

            if (west) {
                i = withDirection(i, Direction.WEST);
            }

            if (east) {
                i = withDirection(i, Direction.EAST);
            }

            return i;
        }

        public static int getFromLevel(long entry) {
            return (int)(entry & 15L);
        }

        public static boolean isFromEmptyShape(long entry) {
            return (entry & 1024L) != 0L;
        }

        public static boolean isIncreaseFromEmission(long entry) {
            return (entry & 2048L) != 0L;
        }

        public static boolean shouldPropagateInDirection(long entry, Direction direction) {
            return (entry & 1L << direction.ordinal() + 4) != 0L;
        }

        private static long withLevel(long entry, int level) {
            return entry & -16L | (long)level & 15L;
        }

        private static long withDirection(long entry, Direction direction) {
            return entry | 1L << direction.ordinal() + 4;
        }

        private static long withoutDirection(long entry, Direction direction) {
            return entry & ~(1L << direction.ordinal() + 4);
        }
    }

    private static record LightUpdate(CoordinateKey pos, long entry) {}
}
