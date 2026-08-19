package org.verselstudios.noisecustomizer.utils;

import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.verselstudios.noisecustomizer.Config;
import org.verselstudios.noisecustomizer.mixin.PerlinNoiseAccessor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.WeakHashMap;

public final class NoiseTracker {

    private static final WeakHashMap<ImprovedNoise, ImprovedNoiseInfo> info = new WeakHashMap<>();
    private static final WeakHashMap<ImprovedNoise, NoiseUsage> usage = new WeakHashMap<>();
    public static final BigDecimal FIFTY_TWO_POW = BigDecimal.valueOf(0x0010_0000_0000_0000L); // 2^52
    public static final BigDecimal MAX_VAL = BigDecimal.valueOf(Long.MAX_VALUE);

    public static void register(
            ImprovedNoise noise,
            int octave,
            double amplitude,
            double coordinateScale
    ) {
        info.put(noise,
                new ImprovedNoiseInfo(octave, amplitude, coordinateScale, noise.xo, noise.yo, noise.zo));
    }

    public static void use(ImprovedNoise noise, double coordinateScaleX, double coordinateScaleY, double coordinateScaleZ, String usage) {
        NoiseTracker.usage.put(noise, new NoiseUsage(coordinateScaleX, coordinateScaleY, coordinateScaleZ, usage));
    }

    public static Map<ImprovedNoise, ImprovedNoiseInfo> metadata() {
        return info;
    }

    public static String report(boolean unknown, boolean blended, boolean normal) {
        StringBuilder builder = new StringBuilder();

        builder.append("=== Improved Noise Report ===\n\n");

        int index = 1;

        synchronized (info) {
            for (Map.Entry<ImprovedNoise, ImprovedNoiseInfo> entry : info.entrySet()) {
                ImprovedNoise noise = entry.getKey();
                ImprovedNoiseInfo info = entry.getValue();

                NoiseUsage current = usage.get(noise);
                if (!unknown && current == null) continue;
                if (!blended && current != null && current.usage().contains("blended")) continue;
                if (!normal && current != null && current.usage().contains("normal")) continue;

                builder.append("Noise #").append(index++).append('\n');

                builder.append("  octave           : ")
                        .append(info.octave())
                        .append('\n');

                builder.append("  amplitude        : ")
                        .append(info.amplitude())
                        .append('\n');

                builder.append("  creation coordinate scale : ")
                        .append(info.coordinateScale())
                        .append('\n');

                builder.append('\n');

                builder.append("  last usage       : ")
                        .append(current == null ? "unknown (using construction scale)" : current.usage())
                        .append('\n');

                if(current != null)
                {
                    builder.append("  effective scale X  : ")
                            .append(current.coordinateScaleX())
                            .append('\n');
                    builder.append("  effective scale Y  : ")
                            .append(current.coordinateScaleY())
                            .append('\n');
                    builder.append("  effective scale Z  : ")
                            .append(current.coordinateScaleZ())
                            .append('\n');

                    builder.append('\n');

                    builder.append("  integer overflow at:\n");

                    appendAxis(builder, "X", current.coordinateScaleX(), info.xo());
                    appendAxis(builder, "Y", current.coordinateScaleY(), info.yo());
                    appendAxis(builder, "Z", current.coordinateScaleZ(), info.zo());

                    builder.append('\n');

                    builder.append("  wrap breakpoints at:\n");

                    appendWrapAxis(builder, "X", current.coordinateScaleX(), info.xo());
                    appendWrapAxis(builder, "Y", current.coordinateScaleY(), info.yo());
                    appendWrapAxis(builder, "Z", current.coordinateScaleZ(), info.zo());

                    builder.append('\n');
                } else {
                    builder.append('\n');

                    builder.append("  integer overflow at:\n");

                    appendAxis(builder, "X", current.coordinateScaleX(), info.xo());
                    appendAxis(builder, "Y", current.coordinateScaleY(), info.yo());
                    appendAxis(builder, "Z", current.coordinateScaleZ(), info.zo());

                    builder.append('\n');

                    builder.append("  wrap breakpoints at:\n");

                    appendWrapAxis(builder, "X", current.coordinateScaleX(), info.xo());
                    appendWrapAxis(builder, "Y", current.coordinateScaleY(), info.yo());
                    appendWrapAxis(builder, "Z", current.coordinateScaleZ(), info.zo());

                    builder.append('\n');
                }

            }
        }

        findFirstBreak(builder, unknown, blended, normal);

        return builder.toString();
    }

    public static void findFirstBreak(StringBuilder builder, boolean unknown, boolean blended, boolean normal) {
        NoiseFailure first = findFirstBreakingNoise(unknown, blended, normal);

        if (first != null) {
            builder.append("\n=== First Expected Failure ===\n\n");

            builder.append("Distance: ")
                    .append(String.format("%,.0f", first.distance()))
                    .append(" blocks\n\n");

            ImprovedNoiseInfo info = first.info();
            NoiseUsage usage = first.usage();

            builder.append("Octave: ")
                    .append(info.octave())
                    .append('\n');

            builder.append("Amplitude: ")
                    .append(info.amplitude())
                    .append('\n');

            builder.append("Usage: ")
                    .append(usage == null ? "unknown" : usage.usage())
                    .append('\n');

            builder.append("Scale:\n");

            if (usage != null) {
                builder.append("  X: ")
                        .append(usage.coordinateScaleX())
                        .append('\n');

                builder.append("  Y: ")
                        .append(usage.coordinateScaleY())
                        .append('\n');

                builder.append("  Z: ")
                        .append(usage.coordinateScaleZ())
                        .append('\n');
                builder.append("\n");

                builder.append("  integer overflow at:\n");

                appendAxis(builder, "X", usage.coordinateScaleX(), info.xo());
                appendAxis(builder, "Y", usage.coordinateScaleY(), info.yo());
                appendAxis(builder, "Z", usage.coordinateScaleZ(), info.zo());

                builder.append('\n');

                builder.append("  wrap breakpoints at:\n");

                appendWrapAxis(builder, "X", usage.coordinateScaleX(), info.xo());
                appendWrapAxis(builder, "Y", usage.coordinateScaleY(), info.yo());
                appendWrapAxis(builder, "Z", usage.coordinateScaleZ(), info.zo());

                builder.append('\n');
            }
            builder.append('\n');
        }
    }

    private static void appendAxis(
            StringBuilder builder,
            String axis,
            double scale,
            double offset
    ) {
        double positive =
                (Integer.MAX_VALUE - offset) / scale;

        double negative =
                (Integer.MIN_VALUE - offset) / scale;

        builder.append("    ")
                .append(axis)
                .append("+: ")
                .append(String.format("%,.0f", positive))
                .append('\n');

        builder.append("    ")
                .append(axis)
                .append("-: ")
                .append(String.format("%,.0f", negative))
                .append('\n');
    }

    private static void appendWrapAxis(
            StringBuilder builder,
            String axis,
            double scale,
            double offset
    ) {
        appendWrapBreakpoint(
                builder,
                axis,
                "break",
                getWrapBreakPoint(),
                getWrapBreakPoint(),
                scale,
                offset
        );

        appendWrapBreakpoint(
                builder,
                axis,
                "saturate",
                getWrapPositiveSaturatePoint(),
                getWrapNegativeSaturatePoint(),
                scale,
                offset
        );

        appendWrapBreakpoint(
                builder,
                axis,
                "expected wrapped breakpoint",
                getWrapPositiveBreakPoint(),
                getWrapNegativeBreakPoint(),
                scale,
                offset
        );
    }

    private static void appendWrapBreakpoint(
            StringBuilder builder,
            String axis,
            String name,
            BigDecimal thresholdPositive,
            BigDecimal thresholdNegative,
            double scale,
            double offset
    ) {
        builder.append("    ")
                .append(axis)
                .append(" ")
                .append(name)
                .append("+ : ");

        if (scale == 0.0) {
            builder.append("never\n");
        } else {
            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            BigDecimal offsetBD = BigDecimal.valueOf(offset);

            BigDecimal positive = thresholdPositive
                    .subtract(offsetBD)
                    .divide(scaleBD, java.math.MathContext.DECIMAL128);

            builder.append(formatBreakpoint(positive))
                    .append('\n');
        }

        builder.append("    ")
                .append(axis)
                .append(" ")
                .append(name)
                .append("- : ");

        if (scale == 0.0) {
            builder.append("never\n");
        } else {
            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            BigDecimal offsetBD = BigDecimal.valueOf(offset);

            BigDecimal negative = thresholdNegative
                    .subtract(offsetBD)
                    .divide(scaleBD, java.math.MathContext.DECIMAL128);

            builder.append(formatBreakpoint(negative))
                    .append('\n');
        }
    }

    private static String formatBreakpoint(BigDecimal value) {
        return String.format(
                "%,d",
                value.setScale(0, java.math.RoundingMode.HALF_UP)
                        .toBigInteger()
        );
    }

    public static void useNormalNoise(NormalNoise normal, double xScale, double yScale, double zScale, String usage) {
        double xl = xScale * Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get();
        double yl = yScale * Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get();
        double zl = zScale * Config.NORMAL_NOISE_FIRST_INPUT_FACTOR.get();
        double xh = xScale * Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get();
        double yh = yScale * Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get();
        double zh = zScale * Config.NORMAL_NOISE_SECOND_INPUT_FACTOR.get();

        trackPerlin(normal.first, xl, yl, zl, usage + ".first");
        trackPerlin(normal.second, xh, yh, zh, usage + ".second");
    }

    public static void trackPerlin(
            PerlinNoise noise,
            double x,
            double y,
            double z,
            String name
    ) {
        PerlinNoiseAccessor accessor = (PerlinNoiseAccessor) noise;

        ImprovedNoise[] levels = accessor.getNoiseLevels();

        double octaveScale = accessor.getLowestFreqInputFactor();

        for (int i = 0; i < levels.length; i++) {
            ImprovedNoise improved = levels[i];

            if (improved != null) {
                NoiseTracker.use(
                        improved,
                        x * octaveScale,
                        y * octaveScale,
                        z * octaveScale,
                        name + ".octave_" + i
                );
            }

            octaveScale *= 2.0;
        }
    }

    public static NoiseFailure findFirstBreakingNoise(boolean unknown, boolean blended, boolean normal) {
        NoiseFailure best = null;

        synchronized (info) {
            for (Map.Entry<ImprovedNoise, ImprovedNoiseInfo> entry : info.entrySet()) {

                ImprovedNoise noise = entry.getKey();
                ImprovedNoiseInfo metadata = entry.getValue();

                NoiseUsage current = usage.get(noise);

                if (!unknown && current == null) continue;
                if (!blended && current != null && current.usage().contains("blended")) continue;
                if (!normal && current != null && current.usage().contains("normal")) continue;

                double xScale = metadata.coordinateScale();
                double yScale = metadata.coordinateScale();
                double zScale = metadata.coordinateScale();

                if (current != null) {
                    xScale = current.coordinateScaleX();
                    yScale = current.coordinateScaleY();
                    zScale = current.coordinateScaleZ();
                }

                double x = Math.min(
                        Math.abs((Integer.MAX_VALUE - metadata.xo()) / xScale),
                        Math.abs((Integer.MIN_VALUE - metadata.xo()) / xScale)
                );

                double y = Math.min(
                        Math.abs((Integer.MAX_VALUE - metadata.yo()) / yScale),
                        Math.abs((Integer.MIN_VALUE - metadata.yo()) / yScale)
                );

                double z = Math.min(
                        Math.abs((Integer.MAX_VALUE - metadata.zo()) / zScale),
                        Math.abs((Integer.MIN_VALUE - metadata.zo()) / zScale)
                );

                double distance = Math.min(x, Math.min(y, z));

                if (best == null || distance < best.distance()) {
                    best = new NoiseFailure(
                            noise,
                            metadata,
                            current,
                            distance
                    );
                }
            }
        }

        return best;
    }

    public record NoiseFailure(
            ImprovedNoise noise,
            ImprovedNoiseInfo info,
            NoiseUsage usage,
            double distance
    ) {}

    public static BigDecimal getWrapBreakPoint() {
        return FIFTY_TWO_POW.add(BigDecimal.ONE).multiply(BigDecimal.valueOf(Config.WRAP_PERIOD.get()));
    }

    public static BigDecimal getWrapPositiveSaturatePoint() {
        return BigDecimal.valueOf(Config.WRAP_PERIOD.get())
                .multiply(BigDecimal.valueOf(Long.MAX_VALUE));
    }

    public static BigDecimal getWrapNegativeSaturatePoint() {
        double period = Config.WRAP_PERIOD.get();

        double saturation =
                period * ((double) Long.MIN_VALUE - 0.5);

        return BigDecimal.valueOf(saturation);
    }

    public static BigDecimal getWrapPositiveBreakPoint() {
        BigDecimal saturation = getWrapPositiveSaturatePoint();

        double saturationDouble = saturation.doubleValue();
        double ulp = Math.ulp(saturationDouble);

        long steps = (long) Math.floor(
                Integer.MAX_VALUE / ulp
        ) + 1L;

        return saturation.add(
                BigDecimal.valueOf(ulp)
                        .multiply(BigDecimal.valueOf(steps))
        );
    }

    public static BigDecimal getWrapNegativeBreakPoint() {
        double period = Config.WRAP_PERIOD.get();

        double saturation =
                period * ((double) Long.MIN_VALUE - 0.5);

        double firstBroken = Math.nextDown(saturation);

        return BigDecimal.valueOf(firstBroken);
    }
}