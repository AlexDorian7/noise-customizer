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
        int offset = Config.GEN_OFFSET.get();
        chunkX += offset;
        chunkZ += offset;
    }

    @Inject(method = "setDecorationSeed", at = @At("HEAD"))
    private void applyOffset(long worldSeed, int blockX, int blockZ, CallbackInfoReturnable<Long> cir) {
        int offset = Config.GEN_OFFSET.get() << 4;
        blockX += offset;
        blockZ += offset;
    }
}
