package org.verselstudios.noisecustomizer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue FIX_SECTIONS = BUILDER
            .comment("Fix the section limit to allow lighting and entities to work past 2^25")
            .define("fixSections", false);


    public static final ModConfigSpec.BooleanValue ENABLE_FARLANDS = BUILDER
            .comment("Enable the 32bit version of the farlands")
            .define("enableFarlands", false);

    public static final ModConfigSpec.IntValue GEN_OFFSET = BUILDER
            .comment("Amount in chunks to offset generation by")
            .defineInRange("genOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue XZ_COORDINATE_SCALE = BUILDER
            .comment("The xz noise coordinate scale")
            .defineInRange("xzCoordinateScale", 171.103, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue Y_COORDINATE_SCALE = BUILDER
            .comment("The y noise coordinate scale")
            .defineInRange("yCoordinateScale", 85.5515, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue LOW_NOISE_SCALE = BUILDER
            .comment("Scale applied to the low noise")
            .defineInRange("lowNoiseScale", 1, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue HIGH_NOISE_SCALE = BUILDER
            .comment("Scale applied to the high noise")
            .defineInRange("highNoiseScale", 1, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue SELECTOR_NOISE_SCALE = BUILDER
            .comment("Scale applied to the selector noise")
            .defineInRange("selectorNoiseScale", 1, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.IntValue NOISE_OCTAVES = BUILDER
            .comment("The amount of octaves to use for low and high noise")
            .defineInRange("noiseOctaves", 16, 0, 16);

    public static final ModConfigSpec.IntValue SELECTOR_NOISE_OCTAVES = BUILDER
            .comment("The amount of octaves to use for selector noise")
            .defineInRange("selectorNoiseOctaves", 8, 0, 8);

    public static final ModConfigSpec.DoubleValue NORMAL_NOISE_INPUT_FACTOR = BUILDER
            .comment("The input factor for new normal noise")
            .defineInRange("normalNoiseInputFactor", 1.0181268882175227, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue NORMAL_NOISE_CLAMP = BUILDER
            .comment("The coordinate to clamp normal noise values at")
            .defineInRange("normalNoiseClamp", Double.MAX_VALUE, 0, Double.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
