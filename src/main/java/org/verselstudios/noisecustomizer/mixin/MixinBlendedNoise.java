package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.*;
import org.verselstudios.noisecustomizer.Config;
import org.verselstudios.noisecustomizer.NoiseCustomizer;

@Mixin(BlendedNoise.class)
public abstract class MixinBlendedNoise {

    @Shadow
    @Final
    private PerlinNoise minLimitNoise;

    @Shadow
    @Final
    private PerlinNoise maxLimitNoise;

    @Shadow
    @Final
    private double xzFactor;

    @Shadow
    @Final
    private double yFactor;

    @Shadow
    @Final
    private double smearScaleMultiplier;

    @Shadow
    @Final
    private PerlinNoise mainNoise;

    @Shadow @Final private double xzMultiplier;
    @Shadow @Final private double yMultiplier;

    /**
     * @author Versel
     * @reason To allow for customizing noise values
     */
    @Overwrite
    public double compute(DensityFunction.FunctionContext context) {
        double d0 = (double)context.blockX() * Config.XZ_COORDINATE_SCALE.get();
        double d1 = (double)context.blockY() * Config.Y_COORDINATE_SCALE.get();
        double d2 = (double)context.blockZ() * Config.XZ_COORDINATE_SCALE.get();
        double d3 = d0 / this.xzFactor;
        double d4 = d1 / this.yFactor;
        double d5 = d2 / this.xzFactor;
        double d6 = Config.Y_COORDINATE_SCALE.get() * this.smearScaleMultiplier;
        double d7 = d6 / this.yFactor;
        double d8 = 0.0;
        double d9 = 0.0;
        double d10 = 0.0;
        boolean flag = true;
        double d11 = 1.0;

        for(int i = 0; i < Config.SELECTOR_NOISE_OCTAVES.get(); ++i) {
            ImprovedNoise improvednoise = this.mainNoise.getOctaveNoise(i);
            if (improvednoise != null) {
                d10 += improvednoise.noise(PerlinNoise.wrap(d3 * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble()), PerlinNoise.wrap(d4 * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble()), PerlinNoise.wrap(d5 * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble()), d7 * d11, d4 * d11) / d11;
            }

            d11 /= 2.0;
        }

        double d16 = (d10 / 10.0 + 1.0) / 2.0;
        boolean flag1 = d16 >= 1.0;
        boolean flag2 = d16 <= 0.0;
        d11 = 1.0;

        for(int j = 0; j < Config.NOISE_OCTAVES.get(); ++j) {
            double d12 = PerlinNoise.wrap(d0 * d11);
            double d13 = PerlinNoise.wrap(d1 * d11);
            double d14 = PerlinNoise.wrap(d2 * d11);
            double d15 = d6 * d11;
            ImprovedNoise improvednoise2;
            if (!flag1) {
                improvednoise2 = this.minLimitNoise.getOctaveNoise(j);
                if (improvednoise2 != null) {
                    d8 += improvednoise2.noise(d12 * Config.LOW_NOISE_SCALE.getAsDouble(), d13 * Config.LOW_NOISE_SCALE.getAsDouble(), d14 * Config.LOW_NOISE_SCALE.getAsDouble(), d15, d1 * d11) / d11;
                }
            }

            if (!flag2) {
                improvednoise2 = this.maxLimitNoise.getOctaveNoise(j);
                if (improvednoise2 != null) {
                    d9 += improvednoise2.noise(d12 * Config.HIGH_NOISE_SCALE.getAsDouble(), d13 * Config.HIGH_NOISE_SCALE.getAsDouble(), d14 * Config.HIGH_NOISE_SCALE.getAsDouble(), d15, d1 * d11) / d11;
                }
            }

            d11 /= 2.0;
        }

        return Mth.clampedLerp(d8 / 512.0, d9 / 512.0, d16) / 128.0;
    }
}
