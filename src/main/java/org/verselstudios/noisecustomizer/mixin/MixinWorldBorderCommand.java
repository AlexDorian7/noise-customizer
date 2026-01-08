package org.verselstudios.noisecustomizer.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Locale;

@Mixin(WorldBorderCommand.class)
public class MixinWorldBorderCommand {
    @Final
    @Shadow
    private static SimpleCommandExceptionType ERROR_SAME_SIZE;
    @Final
    @Shadow
    private static SimpleCommandExceptionType ERROR_TOO_SMALL;
    @Final
    @Shadow
    private static SimpleCommandExceptionType ERROR_TOO_BIG;

    @ModifyConstant(
            constant = @Constant(
                    doubleValue = 5.9999968E7,
                    ordinal = -1
            ),
            method = "register")
    private static double modifyMaxRadius(double original) {
        return Double.MAX_VALUE;
    }

    @ModifyConstant(
            constant = @Constant(
                    doubleValue = -5.9999968E7,
                    ordinal = -1
            ),
            method = "register")
    private static double modifyMinRadius(double original) {
        return -Double.MAX_VALUE;
    }

    /**
     * @author Versel
     * @reason To remove 30 mil border
     */
    @Overwrite
    private static int setSize(CommandSourceStack source, double newSize, long time) throws CommandSyntaxException {
        WorldBorder worldborder = source.getServer().overworld().getWorldBorder();
        double d0 = worldborder.getSize();
        if (d0 == newSize) {
            throw ERROR_SAME_SIZE.create();
        } else if (newSize < 1.0) {
            throw ERROR_TOO_SMALL.create();
        } else {
            if (time > 0L) {
                worldborder.lerpSizeBetween(d0, newSize, time);
                if (newSize > d0) {
                    source.sendSuccess(() -> {
                        return Component.translatable("commands.worldborder.set.grow", new Object[]{String.format(Locale.ROOT, "%.1f", newSize), Long.toString(time / 1000L)});
                    }, true);
                } else {
                    source.sendSuccess(() -> {
                        return Component.translatable("commands.worldborder.set.shrink", new Object[]{String.format(Locale.ROOT, "%.1f", newSize), Long.toString(time / 1000L)});
                    }, true);
                }
            } else {
                worldborder.setSize(newSize);
                source.sendSuccess(() -> {
                    return Component.translatable("commands.worldborder.set.immediate", new Object[]{String.format(Locale.ROOT, "%.1f", newSize)});
                }, true);
            }

            return (int)(newSize - d0);
        }
    }
}
