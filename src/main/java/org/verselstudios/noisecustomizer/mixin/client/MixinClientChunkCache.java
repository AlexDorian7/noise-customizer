package org.verselstudios.noisecustomizer.mixin.client;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.verselstudios.noisecustomizer.Config;
import org.verselstudios.noisecustomizer.overwrites.light.ExtendedLevelLightEngine;

@Mixin(ClientChunkCache.class)
public class MixinClientChunkCache {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/level/lighting/LevelLightEngine"
            )
    )
    private LevelLightEngine replaceLightEngine(
            LightChunkGetter lightChunkGetter, boolean blockLight, boolean skyLight
    ) {
        if (Config.FIX_SECTIONS.get()) { // Config.FIX_SECTIONS.get() // Disabled until light engine can be properly fixed
            return new ExtendedLevelLightEngine(
                    lightChunkGetter,
                    blockLight,
                    skyLight
            );
        } else {
            return new LevelLightEngine(
                    lightChunkGetter,
                    blockLight,
                    skyLight
            );
        }
    }

    @Mixin(ClientChunkCache.Storage.class)
    public static class Storage {
        @Inject(method = "inRange", at = @At("RETURN"), cancellable = true)
        public void inRange(int x, int z, CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(true);
        }

    }
}
