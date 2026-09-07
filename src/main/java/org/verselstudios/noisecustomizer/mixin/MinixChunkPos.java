package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkPos.class)
public class MinixChunkPos {

//    @Unique
//    private static final long COORD_BITS = 16L;
//
//    @Unique
//    private static final long COORD_MASK = (1L << COORD_BITS) - 1L;
//
//    @Unique
//    private static int signExtend16(long value) {
//        int result = (int)(value & COORD_MASK);
//
//        if ((result & 0x8000) != 0) {
//            result |= 0xFFFF0000;
//        }
//
//        return result;
//    }
//
//    @Overwrite
//    public static long asLong(int x, int z) {
//        return ((long)x & COORD_MASK)
//                | (((long)z & COORD_MASK) << COORD_BITS);
//    }
//
//    @Overwrite
//    public static int getX(long chunkAsLong) {
//        return signExtend16(chunkAsLong);
//    }
//
//    @Overwrite
//    public static int getZ(long chunkAsLong) {
//        return signExtend16(chunkAsLong >>> COORD_BITS);
//    }
//
//    @ModifyVariable(
//            method = "<init>(J)V",
//            at = @At("HEAD"),
//            argsOnly = true
//    )
//    private static long modifyPackedPos(long packedPos) {
//        int x = signExtend16(packedPos);
//        int z = signExtend16(packedPos >>> COORD_BITS);
//
//        return ((long)x & 0xFFFFFFFFL)
//                | (((long)z & 0xFFFFFFFFL) << 32);
//    }
}