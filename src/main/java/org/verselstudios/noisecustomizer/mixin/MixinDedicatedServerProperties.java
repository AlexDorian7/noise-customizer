package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DedicatedServerProperties.class)
public class MixinDedicatedServerProperties {
    @ModifyConstant(
            method = "<init>",
            constant = @Constant(intValue = 29999984)
    )
    private int modifyMaxWorldSize(int original) {
        return Integer.MAX_VALUE;
    }

    @ModifyConstant(
            method = "lambda$new$0",
            constant = @Constant(intValue = 29999984)
    )
    private static int modifyMaxWorldSizeLambda(int original) {
        return Integer.MAX_VALUE;
    }
}
