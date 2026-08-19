package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.*;
import org.verselstudios.noisecustomizer.Config;
import org.verselstudios.noisecustomizer.GeneratorType;
import org.verselstudios.noisecustomizer.lua.LuaState;
import org.verselstudios.noisecustomizer.utils.NoiseTracker;

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
        if (Config.NEW_NOISE_TYPE.get().equals(GeneratorType.ONE)) return 1;
        if (Config.NEW_NOISE_TYPE.get().equals(GeneratorType.ZERO)) return 0;
        if (Config.NEW_NOISE_TYPE.get().equals(GeneratorType.LUA)) {
            LuaState.getDefaultInstance().setNoiseFunction(this::noisecustomizer$computeNew);
            return LuaState.getDefaultInstance().normalNoise(x, y, z);
        }
        return noisecustomizer$computeNew(x, y, z);
    }

    @Unique
    public double noisecustomizer$computeNew(double x, double y, double z) {
        double d0, d1, d2;

        x = Mth.clamp(x,
                -Config.NORMAL_NOISE_CLAMP.get(),
                Config.NORMAL_NOISE_CLAMP.get());
        y = Mth.clamp(y,
                -Config.NORMAL_NOISE_CLAMP.get(),
                Config.NORMAL_NOISE_CLAMP.get());
        z = Mth.clamp(z,
                -Config.NORMAL_NOISE_CLAMP.get(),
                Config.NORMAL_NOISE_CLAMP.get());

        d0 = x * Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get();
        d1 = y * Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get();
        d2 = z * Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get();

        // Now tracked in density function
//        NoiseTracker.trackPerlin(this.first,
//                Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get(),
//                Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get(),
//                Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get(),
//                "normal.first");
//
//        NoiseTracker.trackPerlin(this.second,
//                Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get(),
//                Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get(),
//                Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get(),
//                "normal.second");

        return (this.first.getValue(x*Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get(), y*Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get(), z*Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get()) + this.second.getValue(d0, d1, d2)) * this.valueFactor;
    }

}
