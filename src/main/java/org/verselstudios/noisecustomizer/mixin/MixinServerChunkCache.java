package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.verselstudios.noisecustomizer.utils.WorldBounds;

@Mixin(ServerChunkCache.class)
public class MixinServerChunkCache {

    @Shadow @Final public ServerLevel level;

    @Inject(
            method = "getChunk",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventLargeChunks(
            int x,
            int z,
            ChunkStatus status,
            boolean create,
            CallbackInfoReturnable<ChunkAccess> cir
    ) {
        if (!WorldBounds.isChunkAllowed(x, z)) {
            cir.setReturnValue(new ProtoChunk(new ChunkPos(x, z),
                    UpgradeData.EMPTY,
                    level,
                    level.registryAccess().registryOrThrow(Registries.BIOME),
                    null));
        }
    }
}