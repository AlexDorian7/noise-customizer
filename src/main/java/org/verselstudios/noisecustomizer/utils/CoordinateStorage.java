package org.verselstudios.noisecustomizer.utils;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface CoordinateStorage<V> {

    V get(CoordinateKey key);

    V put(CoordinateKey key, V value);

    V remove(CoordinateKey key);

    boolean contains(CoordinateKey key);

    int size();

    void clear();

    Iterable<CoordinateKey> keys();
    Iterable<Entry<V>> entries();

    Iterable<Entry<V>> range(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ
    );

    default Stream<Entry<V>> streamRange(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ
    ) {
        return StreamSupport.stream(
                range(minX,minY,minZ,maxX,maxY,maxZ)
                        .spliterator(),
                false
        );
    }
}