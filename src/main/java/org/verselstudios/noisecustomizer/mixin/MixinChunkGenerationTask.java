package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.verselstudios.noisecustomizer.utils.WorldBounds;

@Mixin(ChunkGenerationTask.class)
public abstract class MixinChunkGenerationTask {

    @Shadow public abstract void markForCancellation();


    @Inject(
            method = "scheduleChunkInLayer",
            at = @At("HEAD"),
            cancellable = true
    )
    public void preventSchedule(ChunkStatus status, boolean needsGeneration, GenerationChunkHolder chunk, CallbackInfoReturnable<Boolean> cir) {
        ChunkPos pos = chunk.getPos();
        if (!WorldBounds.isChunkAllowed(pos.x, pos.z)) {
            markForCancellation();
            cir.setReturnValue(false);
        }
    }
}
