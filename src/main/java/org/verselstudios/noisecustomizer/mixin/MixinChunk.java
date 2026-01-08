package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.verselstudios.noisecustomizer.Config;

@Mixin(ChunkAccess.class)
public class MixinChunk {
    @ModifyVariable(method = "fillBiomesFromNoise", at = @At("STORE"))
    private ChunkPos applyOffset(ChunkPos chunkPos) {
        try {
            int offset = Config.GEN_OFFSET.get();
            return new ChunkPos(chunkPos.x + offset, chunkPos.z + offset);
        } catch (Exception e) {
            return chunkPos;
        }
    }
}
