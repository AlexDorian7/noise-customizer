package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.verselstudios.noisecustomizer.Config;

@Mixin(NormalNoise.class)
public class MixinNormalNoise {

    @Final
    @Shadow
    private PerlinNoise first;

    @Final
    @Shadow
    private PerlinNoise second;

    @Final
    @Shadow
    private double valueFactor;

    /**
     * @author Versel
     * @reason To allow for more noise customizations
     */
    @Overwrite
    public double getValue(double x, double y, double z) {
        double d0, d1, d2;
        try {
            x = Mth.clamp(x, -Config.NORMAL_NOISE_CLAMP.get(), Config.NORMAL_NOISE_CLAMP.get());
            y = Mth.clamp(y, -Config.NORMAL_NOISE_CLAMP.get(), Config.NORMAL_NOISE_CLAMP.get());
            z = Mth.clamp(z, -Config.NORMAL_NOISE_CLAMP.get(), Config.NORMAL_NOISE_CLAMP.get());

            d0 = x * Config.NORMAL_NOISE_INPUT_FACTOR.get();
            d1 = y * Config.NORMAL_NOISE_INPUT_FACTOR.get();
            d2 = z * Config.NORMAL_NOISE_INPUT_FACTOR.get();
        } catch(Exception e) {
            d0 = x * 1.0181268882175227;
            d1 = y * 1.0181268882175227;
            d2 = z * 1.0181268882175227;
        }
        return (this.first.getValue(x, y, z) + this.second.getValue(d0, d1, d2)) * this.valueFactor;
    }
}
