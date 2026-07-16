package org.verselstudios.noisecustomizer.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class TreeCoordinateStorage<V> implements CoordinateStorage<V> {

    private final TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, V>>> storage = new TreeMap<>();

    private int size = 0;

    @Override
    public V get(CoordinateKey key) {
        TreeMap<Integer, TreeMap<Integer, V>> ys = storage.get(key.x());
        if (ys == null) return null;

        TreeMap<Integer, V> zs = ys.get(key.y());
        if (zs == null) return null;

        return zs.get(key.z());
    }

    @Override
    public V put(CoordinateKey key, V value) {
        TreeMap<Integer, TreeMap<Integer, V>> ys =
                storage.computeIfAbsent(key.x(), x -> new TreeMap<>());

        TreeMap<Integer, V> zs =
                ys.computeIfAbsent(key.y(), y -> new TreeMap<>());

        V old = zs.put(key.z(), value);

        if (old == null)
            size++;

        return old;
    }

    @Override
    public V remove(CoordinateKey key) {
        TreeMap<Integer, TreeMap<Integer, V>> ys = storage.get(key.x());
        if (ys == null) return null;

        TreeMap<Integer, V> zs = ys.get(key.y());
        if (zs == null) return null;

        V old = zs.remove(key.z());

        if (old != null) {
            size--;

            if (zs.isEmpty()) {
                ys.remove(key.y());

                if (ys.isEmpty()) {
                    storage.remove(key.x());
                }
            }
        }

        return old;
    }

    @Override
    public boolean contains(CoordinateKey key) {
        return get(key) != null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        storage.clear();
        size = 0;
    }

    @Override
    public Iterable<Entry<V>> range(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ) {

        List<Entry<V>> result = new ArrayList<>();

        for (var xEntry : storage.subMap(minX, true, maxX, true).entrySet()) {

            for (var yEntry : xEntry.getValue().subMap(minY, true, maxY, true).entrySet()) {

                for (var zEntry : yEntry.getValue().subMap(minZ, true, maxZ, true).entrySet()) {

                    CoordinateKey key = new CoordinateKey(
                            xEntry.getKey(),
                            yEntry.getKey(),
                            zEntry.getKey());

                    result.add(new Entry<>(key, zEntry.getValue()));
                }
            }
        }

        return result;
    }

    @Override
    public Iterable<CoordinateKey> keys() {
        return () -> new Iterator<>() {
            private final Iterator<Entry<V>> iterator = entries().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public CoordinateKey next() {
                return iterator.next().key();
            }
        };
    }

    @Override
    public Iterable<Entry<V>> entries() {
        List<Entry<V>> result = new ArrayList<>();

        for (var xEntry : storage.entrySet()) {
            for (var yEntry : xEntry.getValue().entrySet()) {
                for (var zEntry : yEntry.getValue().entrySet()) {

                    result.add(new Entry<>(
                            new CoordinateKey(
                                    xEntry.getKey(),
                                    yEntry.getKey(),
                                    zEntry.getKey()
                            ),
                            zEntry.getValue()
                    ));
                }
            }
        }

        return result;
    }


}