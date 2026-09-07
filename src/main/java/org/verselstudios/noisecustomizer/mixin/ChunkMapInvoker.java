package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public interface ChunkMapInvoker {

    @Invoker("scheduleChunkLoad")
    public CompletableFuture<ChunkAccess> invokeScheduleChunkLoad(ChunkPos pos);

}
