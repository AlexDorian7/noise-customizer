package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.spongepowered.asm.mixin.*;
import org.verselstudios.noisecustomizer.Config;

@Mixin(ImprovedNoise.class)
public abstract class MixinImprovedNoise {

    @Shadow @Final public double xo;
    @Shadow @Final public double yo;
    @Shadow @Final public double zo;

    @Shadow protected abstract int p(int index);

    @Shadow
    private static double gradDot(int gradIndex, double xFactor, double yFactor, double zFactor) {
        return 0;
    }

    @Shadow @Final private static float SHIFT_UP_EPSILON;

    /**
     * @author Versel
     * @reason Add human-readable names
     */
    @Overwrite
    private double sampleAndLerp(int gridX, int gridY, int gridZ, double deltaX, double sampledDeltaY, double deltaZ, double deltaY) {
        int x0Hash = this.p(gridX);
        int x1Hash = this.p(gridX + 1);
        int y0Hash = this.p(x0Hash + gridY);
        int y1Hash = this.p(x0Hash + gridY + 1);
        int y0X1Hash = this.p(x1Hash + gridY);
        int y1X1Hash = this.p(x1Hash + gridY + 1);
        double gradient000 = gradDot(this.p(y0Hash + gridZ), deltaX, sampledDeltaY, deltaZ);
        double gradient100 = gradDot(this.p(y0X1Hash + gridZ), deltaX - 1.0, sampledDeltaY, deltaZ);
        double gradient010 = gradDot(this.p(y1Hash + gridZ), deltaX, sampledDeltaY - 1.0, deltaZ);
        double gradient110 = gradDot(this.p(y1X1Hash + gridZ), deltaX - 1.0, sampledDeltaY - 1.0, deltaZ);
        double gradient001 = gradDot(this.p(y0Hash + gridZ + 1), deltaX, sampledDeltaY, deltaZ - 1.0);
        double gradient101 = gradDot(this.p(y0X1Hash + gridZ + 1), deltaX - 1.0, sampledDeltaY, deltaZ - 1.0);
        double gradient011 = gradDot(this.p(y1Hash + gridZ + 1), deltaX, sampledDeltaY - 1.0, deltaZ - 1.0);
        double gradient111 = gradDot(this.p(y1X1Hash + gridZ + 1), deltaX - 1.0, sampledDeltaY - 1.0, deltaZ - 1.0);
        if (Config.ENABLE_FRINGELANDS.get()) {
            double interpolationX = smoothstep((float) deltaX);
            double interpolationY = smoothstep((float) deltaY);
            double interpolationZ = smoothstep((float) deltaZ);
            return lerp3((float) interpolationX, (float) interpolationY, (float) interpolationZ, (float) gradient000, (float) gradient100, (float) gradient010, (float) gradient110, (float) gradient001, (float) gradient101, (float) gradient011, (float) gradient111);
        } else {
            double interpolationX = Mth.smoothstep(deltaX);
            double interpolationY = Mth.smoothstep(deltaY);
            double interpolationZ = Mth.smoothstep(deltaZ);
            return Mth.lerp3(interpolationX, interpolationY, interpolationZ, gradient000, gradient100, gradient010, gradient110, gradient001, gradient101, gradient011, gradient111);
        }
    }

    /**
     * @author Versel
     * @reason Add human-readable names
     */
    @Overwrite
    public double noise(double xIn, double yIn, double zIn, double yScale, double yMax) {
        double x = xIn + this.xo;
        double y = yIn + this.yo;
        double z = zIn + this.zo;
        int xInt = Mth.floor(x);
        int yInt = Mth.floor(y);
        int zInt = Mth.floor(z);
        double xFrac = x - (double)xInt;
        double yFrac = y - (double)yInt;
        double zFrac = z - (double)zInt;
        double smear;
        if (yScale != 0.0) {
            double smearAmount;
            if (yMax >= 0.0 && yMax < yFrac) {
                smearAmount = yMax;
            } else {
                smearAmount = yFrac;
            }

            smear = (double)Mth.floor(smearAmount / yScale + SHIFT_UP_EPSILON) * yScale;
        } else {
            smear = 0.0;
        }

        return this.sampleAndLerp(xInt, yInt, zInt, xFrac, yFrac - smear, zFrac, yFrac);
    }

    @Unique
    private static float smoothstep(float input) {
        return input * input * input * (input * (input * 6.0f - 15.0f) + 10.0f); // y = 6 x^5 - 15 x^4 + 10 x^3
    }

    @Unique
    private static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    @Unique
    private static float lerp2(float delta1, float delta2, float start1, float end1, float start2, float end2) {
        return lerp(delta2, lerp(delta1, start1, end1), lerp(delta1, start2, end2));
    }

    @Unique
    private static float lerp3(float delta1, float delta2, float delta3, float start1, float end1, float start2, float end2, float start3, float end3, float start4, float end4) {
        return lerp(delta3, lerp2(delta1, delta2, start1, end1, start2, end2), lerp2(delta1, delta2, start3, end3, start4, end4));
    }
}
