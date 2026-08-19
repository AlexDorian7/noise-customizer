package org.verselstudios.noisecustomizer.utils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LiveTracker<T> {

    private final Set<WeakRef<T>> live = ConcurrentHashMap.newKeySet();

    public void register(T obj) {
        live.add(new WeakRef<>(obj));
    }

    public List<T> snapshotLive() {
        List<T> out = new ArrayList<>();

        Iterator<WeakRef<T>> it = live.iterator();

        while (it.hasNext()) {
            WeakRef<T> ref = it.next();

            T value = ref.get();

            if (value == null) {
                it.remove();
            } else {
                out.add(value);
            }
        }

        return out;
    }

    private static final class WeakRef<T> extends WeakReference<T> {

        private final int hash;

        WeakRef(T obj) {
            super(obj);
            this.hash = System.identityHashCode(obj);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof WeakRef<?> other))
                return false;

            return this.get() == other.get();
        }
    }
}