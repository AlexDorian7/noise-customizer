package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SectionPos.class)
public class MixinSectionPos {

    /**
     * @author Versel
     * @reason To fix things breaking at 2^25
     * Note how minecraft tries to store 3 32bit numbers into 1 64bit number... There has to be precision loss somewhere
     */
    @Overwrite
    public static long asLong(int x, int y, int z) {

        long i = 0L;
        i |= ((long)x & 0x3FF_FFFFL) << 38;
        i |= ((long)y & 0xFFFL) << 0;
        return i | ((long)z & 0x3FF_FFFFL) << 12;
    }

    /**
     * @author Versel
     * @reason To fix things breaking at 2^25
     */
    @Overwrite
    public static int x(long packed) {
        return (int)(packed << 0 >> 38);
    }

    /**
     * @author Versel
     * @reason To fix things breaking at 2^25
     */
    @Overwrite
    public static int y(long packed) {
        return (int)(packed << 52 >> 52);
    }

    /**
     * @author Versel
     * @reason To fix things breaking at 2^25
     */
    @Overwrite
    public static int z(long packed) {
        return (int)(packed << 26 >> 38);
    }
}
