package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor {
    @Inject(method = "isInWorldBoundsHorizontal", at = @At(value = "RETURN"), cancellable = true)
    private static void isInWorldBoundsHorizontal(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isOutsideSpawnableHeight", at = @At(value = "RETURN"), cancellable = true)
    private static void isOutsideSpawnableHeight(int y, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    /**
     * @author Versel
     * @reason To remove world border bounds
     */
    @Overwrite
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        int i;
            if (this.hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) {
                i = this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).getHeight(heightmapType, x & 15, z & 15) + 1;
            } else {
                i = this.getMinBuildHeight();
            }

        return i;
    }
}
