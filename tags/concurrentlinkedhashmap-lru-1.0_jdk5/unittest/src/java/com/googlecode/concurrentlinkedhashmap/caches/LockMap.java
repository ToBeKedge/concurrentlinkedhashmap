package com.googlecode.concurrentlinkedhashmap.caches;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A self-evicting map that is protected by reentrant locks. Operates in LRU or FIFO mode.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
final class LockMap<K, V> extends UnsafeMap<K, V> {
  private static final long serialVersionUID = 1L;
  private final Lock readLock;
  private final Lock writeLock;

  /**
   * @param accessOrder The eviction policy: true=LRU, false=FIFO.
   * @param capacity    The maximum capacity of the map.
   */
  public LockMap(boolean accessOrder, int capacity) {
    super(accessOrder, capacity);
    if (accessOrder) {
      // LRU mutates on reads to update access order
      readLock = writeLock = new ReentrantLock();
    } else {
      ReadWriteLock lock = new ReentrantReadWriteLock();
      readLock = lock.readLock();
      writeLock = lock.writeLock();
    }
  }

  @Override
  public V get(Object key) {
    readLock.lock();
    try {
      return super.get(key);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public V put(K key, V value) {
    writeLock.lock();
    try {
      return super.put(key, value);
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public int size() {
    readLock.lock();
    try {
      return super.size();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void clear() {
    writeLock.lock();
    try {
      super.clear();
    } finally {
      writeLock.unlock();
    }
  }
}
