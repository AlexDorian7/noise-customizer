package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.verselstudios.noisecustomizer.Config;
import org.verselstudios.noisecustomizer.GeneratorType;
import org.verselstudios.noisecustomizer.lua.LuaState;
import org.verselstudios.noisecustomizer.utils.NoiseTracker;

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
        if (Config.OLD_NOISE_TYPE.get().equals(GeneratorType.ONE)) return 1;
        if (Config.OLD_NOISE_TYPE.get().equals(GeneratorType.ZERO)) return 0;
        double x = context.blockX() * Config.XZ_COORDINATE_SCALE.get();
        double y = context.blockY() * Config.Y_COORDINATE_SCALE.get();
        double z = context.blockZ() * Config.XZ_COORDINATE_SCALE.get();
        if (Config.OLD_NOISE_TYPE.get().equals(GeneratorType.LUA)) {
            LuaState.getDefaultInstance().setNoiseFunction(this::noisecustomizer$computeOld);
            return LuaState.getDefaultInstance().blendedNoise(x, y, z);
        }
        return noisecustomizer$computeOld(context.blockX(), context.blockY(), context.blockZ());
    }

    @Unique
    public double noisecustomizer$computeOld(double x, double y, double z) {
        x *= Config.XZ_COORDINATE_SCALE.get();
        y *= Config.Y_COORDINATE_SCALE.get();
        z *= Config.XZ_COORDINATE_SCALE.get();
        double xzFactor = Config.XZ_COORDINATE_FACTOR.get() == 0 ? this.xzFactor : Config.XZ_COORDINATE_FACTOR.get();
        double yFactor = Config.Y_COORDINATE_FACTOR.get() == 0 ? this.yFactor : Config.Y_COORDINATE_FACTOR.get();
        double sx = x / xzFactor;
        double sy = y / yFactor;
        double sz = z / xzFactor;
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
                NoiseTracker.use(improvednoise, Config.XZ_COORDINATE_SCALE.get() / xzFactor * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble(), Config.Y_COORDINATE_SCALE.get() / yFactor * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble(), Config.XZ_COORDINATE_SCALE.get() / xzFactor * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble(), "blended.selector.octave_" + i);
                d10 += improvednoise.noise(PerlinNoise.wrap(sx * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble()), PerlinNoise.wrap(sy * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble()), PerlinNoise.wrap(sz * d11 * Config.SELECTOR_NOISE_SCALE.getAsDouble()), d7 * d11, sy * d11) / d11;
            }

            d11 /= 2.0;
        }

        double d16 = (d10 / 10.0 + 1.0) / 2.0;
        boolean flag1 = d16 >= 1.0;
        boolean flag2 = d16 <= 0.0;
        d11 = 1.0;

        for(int j = 0; j < Config.NOISE_OCTAVES.get(); ++j) {
            double d12 = PerlinNoise.wrap(x * d11);
            double d13 = PerlinNoise.wrap(y * d11);
            double d14 = PerlinNoise.wrap(z * d11);
            double d15 = d6 * d11;
            ImprovedNoise improvednoise2;
            if (!flag1) {
                improvednoise2 = this.minLimitNoise.getOctaveNoise(j);
                if (improvednoise2 != null) {
                    NoiseTracker.use(improvednoise2, Config.XZ_COORDINATE_SCALE.get() * d11 * Config.LOW_NOISE_SCALE.getAsDouble(), Config.Y_COORDINATE_SCALE.get() * d11 * Config.LOW_NOISE_SCALE.getAsDouble(), Config.XZ_COORDINATE_SCALE.get() * d11 * Config.LOW_NOISE_SCALE.getAsDouble(), "blended.low.octave_" + j);
                    d8 += improvednoise2.noise(d12 * Config.LOW_NOISE_SCALE.getAsDouble(), d13 * Config.LOW_NOISE_SCALE.getAsDouble(), d14 * Config.LOW_NOISE_SCALE.getAsDouble(), d15, y * d11) / d11;
                }
            }

            if (!flag2) {
                improvednoise2 = this.maxLimitNoise.getOctaveNoise(j);
                if (improvednoise2 != null) {
                    NoiseTracker.use(improvednoise2, Config.XZ_COORDINATE_SCALE.get() * d11 * Config.HIGH_NOISE_SCALE.getAsDouble(), Config.Y_COORDINATE_SCALE.get() * d11 * Config.HIGH_NOISE_SCALE.getAsDouble(), Config.XZ_COORDINATE_SCALE.get() * d11 * Config.HIGH_NOISE_SCALE.getAsDouble(), "blended.high.octave_" + j);
                    d9 += improvednoise2.noise(d12 * Config.HIGH_NOISE_SCALE.getAsDouble(), d13 * Config.HIGH_NOISE_SCALE.getAsDouble(), d14 * Config.HIGH_NOISE_SCALE.getAsDouble(), d15, y * d11) / d11;
                }
            }

            d11 /= 2.0;
        }

        return Mth.clampedLerp(d8 / 512.0, d9 / 512.0, d16) / 128.0;
    }

//    @Inject(method = "<init>(Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;Lnet/minecraft/world/level/levelgen/synth/PerlinNoise;DDDDD)V", at = @At("RETURN"))
//    public void construct(PerlinNoise minLimitNoise, PerlinNoise maxLimitNoise, PerlinNoise mainNoise, double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier, CallbackInfo ci) {
//        NoiseTracker.register((BlendedNoise) (Object) this);
//    }
}
