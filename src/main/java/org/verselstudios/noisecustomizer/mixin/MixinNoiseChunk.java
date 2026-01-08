package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.verselstudios.noisecustomizer.Config;

@Mixin(NoiseChunk.class)
public class MixinNoiseChunk {
    @Redirect(method = "forChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getPos()Lnet/minecraft/world/level/ChunkPos;"))
    private static ChunkPos applyOffset(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int offset = Config.GEN_OFFSET.get();
        return new ChunkPos(chunkPos.x + offset, chunkPos.z + offset);
    }
}
