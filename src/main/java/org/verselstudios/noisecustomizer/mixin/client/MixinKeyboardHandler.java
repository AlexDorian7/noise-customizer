package org.verselstudios.noisecustomizer.mixin.client;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {

    @Inject(
            method = "handleDebugKeys",
            at = @At("HEAD"),
            cancellable = true
    )
    private void enableChunkDebugKeys(
            int key,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (((KeyboardHandlerInvoker) this).invokeHandleChunkDebugKeys(key)) {
            cir.setReturnValue(true);
        }
    }
}