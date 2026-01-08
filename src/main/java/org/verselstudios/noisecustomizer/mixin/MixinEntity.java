package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(Entity.class)
public abstract class MixinEntity {
    @Redirect(method = "absMoveTo(DDD)V", at = @At(target = "Lnet/minecraft/util/Mth;clamp(DDD)D", value = "INVOKE"))
    private double redirectClamp(double value, double min, double max) {
        return value;
    }

    @Redirect(method = "load", at = @At(target = "Lnet/minecraft/util/Mth;clamp(DDD)D", value = "INVOKE"))
    private double redirectClampReadNbt(double value, double min, double max) {
        return value;
    }
}
