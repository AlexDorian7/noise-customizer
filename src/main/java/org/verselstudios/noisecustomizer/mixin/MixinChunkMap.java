package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.server.level.*;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.verselstudios.noisecustomizer.overwrites.light.ExtendedThreadedLevelLightEngine;

@Mixin(ChunkMap.class)
public class MixinChunkMap {
    @Shadow @Final private ServerLevel level;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/server/level/ThreadedLevelLightEngine"
            )
    )
    private ThreadedLevelLightEngine replaceLightEngine(
            LightChunkGetter lightChunk,
            ChunkMap chunkMap,
            boolean hasSkyLight,
            ProcessorMailbox<Runnable> mailbox,
            ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> taskExecutor
    ) {
        if (false) { // Config.FIX_SECTIONS.get() // Disabled until light engine can be properly fixed
            return new ExtendedThreadedLevelLightEngine(
                    lightChunk,
                    chunkMap,
                    hasSkyLight,
                    mailbox,
                    taskExecutor
            );
        } else {
            return new ThreadedLevelLightEngine(
                    lightChunk,
                    chunkMap,
                    hasSkyLight,
                    mailbox,
                    taskExecutor
            );
        }
    }
}
