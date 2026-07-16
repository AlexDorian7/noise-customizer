package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.verselstudios.noisecustomizer.utils.WorldBounds;

@Mixin(WorldGenRegion.class)
public class MixinWorldgenRegion {

    @Inject(
            method = "getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventLargeGeneration(
            int x,
            int z,
            ChunkStatus status,
            boolean require,
            CallbackInfoReturnable<ChunkAccess> cir
    ) {
        if (!WorldBounds.isChunkAllowed(x, z)) {
            System.out.println(
                    x + "," + z +
                            " status=" + status
            );
            cir.cancel();
        }
    }
}
