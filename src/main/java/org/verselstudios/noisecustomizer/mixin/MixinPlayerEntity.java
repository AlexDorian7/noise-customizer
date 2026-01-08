package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class MixinPlayerEntity {

    @Redirect(method = "tick", at = @At(target = "Lnet/minecraft/util/Mth;clamp(DDD)D", value = "INVOKE"))
    private double redirectClamp(double value, double min, double max) {
        return value;
    }
}
