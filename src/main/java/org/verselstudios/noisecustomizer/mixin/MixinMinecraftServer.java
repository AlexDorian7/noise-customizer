package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(method = "getAbsoluteMaxWorldSize", at = @At("HEAD"), cancellable = true)
    public void getAbsoluteMaxWorldSize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Integer.MAX_VALUE);
        cir.cancel();
    }
}
