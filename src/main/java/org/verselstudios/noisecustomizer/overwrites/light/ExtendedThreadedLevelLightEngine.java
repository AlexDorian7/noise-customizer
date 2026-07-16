package org.verselstudios.noisecustomizer.overwrites.light;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

public class ExtendedThreadedLevelLightEngine extends ThreadedLevelLightEngine implements AutoCloseable {
    public static final int DEFAULT_BATCH_SIZE = 1000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ProcessorMailbox<Runnable> taskMailbox;
    private final ObjectList<Pair<TaskType, Runnable>> lightTasks = new ObjectArrayList<>();
    private final ChunkMap chunkMap;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> sorterMailbox;
    private final int taskPerBatch = 1000;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    private final ExtendedLevelLightEngine engine;
    
    public ExtendedThreadedLevelLightEngine(
            LightChunkGetter lightChunk,
            ChunkMap chunkMap,
            boolean skyLight,
            ProcessorMailbox<Runnable> taskMailbox,
            ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> sorterMailbox
    ) {
        super(lightChunk, chunkMap, skyLight, taskMailbox, sorterMailbox);
        this.chunkMap = chunkMap;
        this.sorterMailbox = sorterMailbox;
        this.taskMailbox = taskMailbox;
        engine = new ExtendedLevelLightEngine(lightChunk, true, skyLight);
        
    }

    @Override
    public void close() {
    }

    @Override
    public int runLightUpdates() {
        throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
    }

    @Override
    public void checkBlock(BlockPos pos) {
        BlockPos blockpos = pos.immutable();
        this.addTask(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()),
                ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE,
                Util.name(() -> engine.checkBlock(blockpos), () -> "checkBlock " + blockpos)
        );
    }

    protected void updateChunkStatus(ChunkPos chunkPos) {
        this.addTask(chunkPos.x, chunkPos.z, () -> 0, ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
            engine.retainData(chunkPos, false);
            engine.setLightEnabled(chunkPos, false);

            for (int i = this.getMinLightSection(); i < this.getMaxLightSection(); i++) {
                engine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, i), null);
                engine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, i), null);
            }

            for (int j = this.levelHeightAccessor.getMinSection(); j < this.levelHeightAccessor.getMaxSection(); j++) {
                engine.updateSectionStatus(SectionPos.of(chunkPos, j), true);
            }
        }, () -> "updateChunkStatus " + chunkPos + " true"));
    }

    @Override
    public void updateSectionStatus(SectionPos pos, boolean isEmpty) {
        this.addTask(
                pos.x(),
                pos.z(),
                () -> 0,
                ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE,
                Util.name(() -> engine.updateSectionStatus(pos, isEmpty), () -> "updateSectionStatus " + pos + " " + isEmpty)
        );
    }

    @Override
    public void propagateLightSources(ChunkPos chunkPos) {
        this.addTask(
                chunkPos.x,
                chunkPos.z,
                ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE,
                Util.name(() -> engine.propagateLightSources(chunkPos), () -> "propagateLight " + chunkPos)
        );
    }

    @Override
    public void setLightEnabled(ChunkPos chunkPos, boolean lightEnabled) {
        this.addTask(
                chunkPos.x,
                chunkPos.z,
                ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE,
                Util.name(() -> engine.setLightEnabled(chunkPos, lightEnabled), () -> "enableLight " + chunkPos + " " + lightEnabled)
        );
    }

    @Override
    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer) {
        this.addTask(
                sectionPos.x(),
                sectionPos.z(),
                () -> 0,
                ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE,
                Util.name(() -> engine.queueSectionData(lightLayer, sectionPos, dataLayer), () -> "queueData " + sectionPos)
        );
    }

    private void addTask(int chunkX, int chunkZ, ExtendedThreadedLevelLightEngine.TaskType type, Runnable task) {
        this.addTask(chunkX, chunkZ, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong(chunkX, chunkZ)), type, task);
    }

    private void addTask(int chunkX, int chunkZ, IntSupplier queueLevelSupplier, ExtendedThreadedLevelLightEngine.TaskType type, Runnable task) {
        this.sorterMailbox.tell(ChunkTaskPriorityQueueSorter.message(() -> {
            this.lightTasks.add(Pair.of(type, task));
            if (this.lightTasks.size() >= 1000) {
                this.runUpdate();
            }
        }, ChunkPos.asLong(chunkX, chunkZ), queueLevelSupplier));
    }

    @Override
    public void retainData(ChunkPos pos, boolean retain) {
        this.addTask(
                pos.x,
                pos.z,
                () -> 0,
                ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE,
                Util.name(() -> engine.retainData(pos, retain), () -> "retainData " + pos)
        );
    }

    public CompletableFuture<ChunkAccess> initializeLight(ChunkAccess chunk, boolean lightEnabled) {
        ChunkPos chunkpos = chunk.getPos();
        this.addTask(chunkpos.x, chunkpos.z, ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
            LevelChunkSection[] alevelchunksection = chunk.getSections();

            for (int i = 0; i < chunk.getSectionsCount(); i++) {
                LevelChunkSection levelchunksection = alevelchunksection[i];
                if (!levelchunksection.hasOnlyAir()) {
                    int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
                    engine.updateSectionStatus(SectionPos.of(chunkpos, j), false);
                }
            }
        }, () -> "initializeLight: " + chunkpos));
        return CompletableFuture.supplyAsync(() -> {
            engine.setLightEnabled(chunkpos, lightEnabled);
            engine.retainData(chunkpos, false);
            return chunk;
        }, p_215135_ -> this.addTask(chunkpos.x, chunkpos.z, ExtendedThreadedLevelLightEngine.TaskType.POST_UPDATE, p_215135_));
    }

    public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess chunk, boolean isLighted) {
        ChunkPos chunkpos = chunk.getPos();
        chunk.setLightCorrect(false);
        this.addTask(chunkpos.x, chunkpos.z, ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
            if (!isLighted) {
                engine.propagateLightSources(chunkpos);
            }
        }, () -> "lightChunk " + chunkpos + " " + isLighted));
        return CompletableFuture.supplyAsync(() -> {
            chunk.setLightCorrect(true);
            return chunk;
        }, p_280982_ -> this.addTask(chunkpos.x, chunkpos.z, ExtendedThreadedLevelLightEngine.TaskType.POST_UPDATE, p_280982_));
    }

    public void tryScheduleUpdate() {
        if ((!this.lightTasks.isEmpty() || engine.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
            this.taskMailbox.tell(() -> {
                this.runUpdate();
                this.scheduled.set(false);
            });
        }
    }

    private void runUpdate() {
        int i = Math.min(this.lightTasks.size(), 1000);
        ObjectListIterator<Pair<ExtendedThreadedLevelLightEngine.TaskType, Runnable>> objectlistiterator = this.lightTasks.iterator();

        int j;
        for (j = 0; objectlistiterator.hasNext() && j < i; j++) {
            Pair<ExtendedThreadedLevelLightEngine.TaskType, Runnable> pair = objectlistiterator.next();
            if (pair.getFirst() == ExtendedThreadedLevelLightEngine.TaskType.PRE_UPDATE) {
                pair.getSecond().run();
            }
        }

        objectlistiterator.back(j);
        engine.runLightUpdates();

        for (int k = 0; objectlistiterator.hasNext() && k < i; k++) {
            Pair<ExtendedThreadedLevelLightEngine.TaskType, Runnable> pair1 = objectlistiterator.next();
            if (pair1.getFirst() == ExtendedThreadedLevelLightEngine.TaskType.POST_UPDATE) {
                pair1.getSecond().run();
            }

            objectlistiterator.remove();
        }
    }

    public CompletableFuture<?> waitForPendingTasks(int x, int z) {
        return CompletableFuture.runAsync(() -> {
        }, p_300778_ -> this.addTask(x, z, ExtendedThreadedLevelLightEngine.TaskType.POST_UPDATE, p_300778_));
    }

    static enum TaskType {
        PRE_UPDATE,
        POST_UPDATE;
    }
}
