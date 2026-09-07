package org.verselstudios.noisecustomizer.mixin.client;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyboardHandler.class)
public interface KeyboardHandlerInvoker {

    @Invoker("handleChunkDebugKeys")
    boolean invokeHandleChunkDebugKeys(int keyCode);
}