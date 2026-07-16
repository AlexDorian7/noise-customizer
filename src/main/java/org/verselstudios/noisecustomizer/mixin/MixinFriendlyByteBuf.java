package org.verselstudios.noisecustomizer.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FriendlyByteBuf.class)
public abstract class MixinFriendlyByteBuf {


    @Shadow
    public abstract int readInt();

    @Shadow public abstract FriendlyByteBuf writeInt(int value);

    /**
     * @author Versel
     * @reason replace packed longs
     */
    @Overwrite
    public static BlockPos readBlockPos(ByteBuf buffer) {
        return new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    /**
     * @author Versel
     * @reason replace packed longs
     */
    @Overwrite
    public static void writeBlockPos(ByteBuf buffer, BlockPos pos) {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());
    }

    /**
     * @author Versel
     * @reason replace packed longs
     */
    @Overwrite
    public SectionPos readSectionPos() {
        return SectionPos.of(readInt(), readInt(), readInt());
    }

    /**
     * @author Versel
     * @reason replace packed longs
     */
    @Overwrite
    public FriendlyByteBuf writeSectionPos(SectionPos sectionPos) {
        writeInt(sectionPos.x());
        writeInt(sectionPos.y());
        writeInt(sectionPos.z());
        return (FriendlyByteBuf)(Object)this;
    }

}
