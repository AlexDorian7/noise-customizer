package org.verselstudios.noisecustomizer.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DimensionType.class)
public abstract class MixinDimensionType {
    @Redirect(method = "lambda$static$0", at = @At(target = "Lcom/mojang/serialization/Codec;doubleRange(DD)Lcom/mojang/serialization/Codec;", value = "INVOKE"))
    private static Codec<Double> fixRange(double min, double max) {
        return Codec.doubleRange(min, Double.MAX_VALUE);
    }
}
