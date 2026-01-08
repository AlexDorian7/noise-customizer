package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockPos.class)
public class MixinBlockPos {
    @Redirect(method = "<clinit>", at = @At(target = "Lnet/minecraft/util/Mth;smallestEncompassingPowerOfTwo(I)I", value = "INVOKE"))
    private static int getPackedXLength(int value) {
        return 512; //Mth.smallestEncompassingPowerOfTwo(10_000);
    }
}
