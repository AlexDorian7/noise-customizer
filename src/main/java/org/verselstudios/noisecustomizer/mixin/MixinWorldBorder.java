package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder {
    @Shadow int absoluteMaxSize;

    @Shadow public abstract void setSize(double size);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void handleConstructor(CallbackInfo ci) {
        this.absoluteMaxSize = Integer.MAX_VALUE;
        setSize(4294967294D);
    }
}
