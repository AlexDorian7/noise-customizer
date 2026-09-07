package org.verselstudios.noisecustomizer.mixin.client;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkCache.Storage.class)
public class MixinClientChunkCache {
    @Inject(method = "inRange", at = @At("RETURN"), cancellable = true)
    public void inRange(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
