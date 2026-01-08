package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.verselstudios.noisecustomizer.Config;

@Mixin(PerlinNoise.class)
public abstract class MixinPerlinNoise {

    @Inject(at = @At(value = "RETURN"), method = "wrap", cancellable = true)
    private static void farlandsFix(double value, CallbackInfoReturnable<Double> cir) {
        if (!Config.ENABLE_FARLANDS.get()) return;
        cir.setReturnValue(value);
    }

}
