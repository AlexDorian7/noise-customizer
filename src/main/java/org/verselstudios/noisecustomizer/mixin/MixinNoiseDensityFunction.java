package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.verselstudios.noisecustomizer.utils.NoiseTracker;

@Mixin(DensityFunctions.Noise.class)
public class MixinNoiseDensityFunction {

    @Shadow
    private DensityFunction.NoiseHolder noise;

    @Shadow
    private double xzScale;

    @Shadow
    private double yScale;


    @Inject(
            method = "compute",
            at = @At("HEAD")
    )
    private void noisecustomizer$trackUsage(
            DensityFunction.FunctionContext context,
            CallbackInfoReturnable<Double> cir
    ) {

        double xScale = this.xzScale;
        double yScale = this.yScale;
        double zScale = this.xzScale;


        NormalNoise normal =
                ((DensityFunctions.Noise) (Object) this).noise().noise();

        NoiseTracker.useNormalNoise(
                normal,
                xScale,
                yScale,
                zScale,
                "density_function.noise.normal." + noisecustomizer$getNoiseName()
        );
    }

    @Unique
    private String noisecustomizer$getNoiseName() {
        DensityFunction.NoiseHolder holder = this.noise;

        return holder.noiseData().unwrapKey().map(key -> key.location().toString()).orElse("unknown");
    }
}