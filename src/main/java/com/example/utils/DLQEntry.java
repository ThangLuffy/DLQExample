package com.example.utils;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DLQEntry<K> implements Delayed {
    private final K key;
    private final long expirationTime;

    private final Runnable runnable;

    public DLQEntry(K key, Runnable runnable) {
        this.key = key;
        this.runnable = runnable;
        this.expirationTime = 0;
    }

    public DLQEntry(K key, long delay, TimeUnit timeUnit, Runnable runnable) {
        this.key = key;
        this.runnable = runnable;
        this.expirationTime = System.currentTimeMillis() + timeUnit.toMillis(delay);
    }

    public K getKey() {
        return key;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expirationTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
        return Long.compare(diff, 0);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DLQEntry<?> other) {
            return key.equals(other.key);
        }
        return false;
    }

    public void apply() {
        runnable.run();
    }
}