package org.verselstudios.noisecustomizer.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.verselstudios.noisecustomizer.Config;
import org.verselstudios.noisecustomizer.utils.NoiseTracker;

@Mixin(PerlinNoise.class)
public abstract class MixinPerlinNoise {

    @Inject(method = "wrap", at = @At("RETURN"), cancellable = true)
    private static void wrap(double value, CallbackInfoReturnable<Double> cir) {
        if (Config.ENABLE_FARLANDS.get()) {
            cir.setReturnValue(value);
            return;
        }
        double v = value - (double) Mth.lfloor(value / Config.WRAP_PERIOD.get() + 0.5) * Config.WRAP_PERIOD.get();
        cir.setReturnValue(v);
    }


    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void registerNoiseLevels(
            RandomSource random,
            Pair<Integer, DoubleList> octavesAndAmplitudes,
            boolean useNewFactory,
            CallbackInfo ci
    ) {
        PerlinNoiseAccessor self = (PerlinNoiseAccessor) this;

        ImprovedNoise[] levels = self.getNoiseLevels();
        DoubleList amplitudes = self.getAmplitudes();

        double scale = self.getLowestFreqInputFactor();
        int octave = self.getFirstOctave();

        for (int i = 0; i < levels.length; i++) {

            ImprovedNoise noise = levels[i];

            if (noise != null) {
                NoiseTracker.register(
                        noise,
                        octave,
                        amplitudes.getDouble(i),
                        scale
                );
            }

            octave++;
            scale *= 2.0;
        }
    }
}
