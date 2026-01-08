package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {

    /**
     * @author Versel
     * @reason To remove 30 mil border
     */
    @Overwrite
    private static double clampHorizontal(double value) {
        return value;
    }

    /**
     * @author Versel
     * @reason To remove 30 mil border
     */
    @Overwrite
    private static double clampVertical(double value) {
        return value;
    }
}
