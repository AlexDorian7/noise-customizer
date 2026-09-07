package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.verselstudios.noisecustomizer.Config;

@Mixin(WorldgenRandom.class)
public class MixinWorldgenRandom {
    @Inject(method = "setLargeFeatureSeed", at = @At("HEAD"))
    private void applyOffset(long worldSeed, int chunkX, int chunkZ, CallbackInfo ci) {
        int offsetX = Config.getChunkX(chunkX >> 4);
        int offsetZ = Config.getChunkX(chunkZ >> 4);
        chunkX += offsetX;
        chunkZ += offsetZ;
    }

    @Inject(method = "setDecorationSeed", at = @At("HEAD"))
    private void applyOffset(long worldSeed, int blockX, int blockZ, CallbackInfoReturnable<Long> cir) {
        int offsetX = Config.getChunkX(blockX >> 4) << 4;
        int offsetZ = Config.getChunkX(blockZ >> 4) << 4;
        blockX += offsetX;
        blockZ += offsetZ;
    }
}
