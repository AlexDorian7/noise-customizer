package org.verselstudios.noisecustomizer.utils;

import it.unimi.dsi.fastutil.longs.LongConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

import java.util.function.Consumer;

import static net.minecraft.core.SectionPos.blockToSectionCoord;

public record CoordinateKey(int x, int y, int z) {
    public static CoordinateKey fromBlock(BlockPos pos) {
        return new CoordinateKey(pos.getX(), pos.getY(), pos.getZ());
    }
    public static CoordinateKey fromSection(SectionPos pos) {
        return new CoordinateKey(pos.x(), pos.y(), pos.z());
    }
    public static CoordinateKey fromZeroSection(SectionPos pos) {
        return new CoordinateKey(pos.x(), 0, pos.z());
    }
    public static CoordinateKey fromChunk(ChunkPos pos) {
        return new CoordinateKey(pos.x, 0, pos.z);
    }

    public CoordinateKey offset(int x, int y, int z) {
        return new CoordinateKey(this.x + x, this.y + y, this.z + z);
    }

    public CoordinateKey toSection() {
        return new CoordinateKey(this.x >> 4, this.y >> 4, this.z >> 4);
    }

    public CoordinateKey toColumn() {
        return new CoordinateKey(this.x, 0, this.z);
    }

    public CoordinateKey getZeroNode() {
        return toColumn();
    }

    public CoordinateKey sectionRelative() {
        return new CoordinateKey(this.x & 15, this.y & 15, this.z & 15);
    }

    public CoordinateKey offset(Direction direction) {
        return this.offset(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public CoordinateKey getFlatIndex() {
        return new CoordinateKey(x, y & 0xFFFF_FFF0, z);
    }

    public CoordinateKey sectionToBlock() {
        return new CoordinateKey(
                x << 4,
                y << 4,
                z << 4
        );
    }

    public static void aroundAndAt(CoordinateKey levelPos, Consumer<CoordinateKey> consumer) {
        aroundAndAt(levelPos.x(), levelPos.y(), levelPos.z(), consumer);
    }

    public static void aroundAndAt(int x, int y, int z, Consumer<CoordinateKey> consumer) {
        int i = blockToSectionCoord(x - 1);
        int j = blockToSectionCoord(x + 1);
        int k = blockToSectionCoord(y - 1);
        int l = blockToSectionCoord(y + 1);
        int i1 = blockToSectionCoord(z - 1);
        int j1 = blockToSectionCoord(z + 1);
        if (i == j && k == l && i1 == j1) {
            consumer.accept(new CoordinateKey(i, k, i1));
        } else {
            for(int k1 = i; k1 <= j; ++k1) {
                for(int l1 = k; l1 <= l; ++l1) {
                    for(int i2 = i1; i2 <= j1; ++i2) {
                        consumer.accept(new CoordinateKey(k1, l1, i2));
                    }
                }
            }
        }

    }
}