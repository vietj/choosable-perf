package de.codepitbull.vertx.perf.others;

import io.vertx.core.spi.cluster.ChoosableIterable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CASOptChoosable<T> implements ChoosableIterable<T> {

  private volatile T[] elements;
  private final BackOffAtomicLong index = new BackOffAtomicLong(); // Could be optimized with false sharing but well...

  public CASOptChoosable(List<T> elements) {
    this.elements = (T[]) elements.toArray();
  }

  public synchronized void add(T t) {
    for (int i = 0;i < elements.length;i++) {
      if (elements[i].equals(t)) {
        return;
      }
    }
    T[] copy = Arrays.copyOf(elements, elements.length + 1);
    copy[elements.length] = t;
    elements = copy;
  }

  public synchronized void remove(T t) {
    for (int i = 0;i < elements.length;i++) {
      if (elements[i].equals(t)) {
        T[] copy = (T[]) new Object[elements.length - 1];
        System.arraycopy(elements, 0, copy, 0, i);
        System.arraycopy(elements, i + 1, copy, i, elements.length - i);
      }
    }
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T choose() {
    T[] current = elements;
    long next = index.incrementAndGet();
    return current[(int)(next % current.length)];
  }

  @Override
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException();
  }

  class BackOffAtomicLong {

    private final AtomicLong value = new AtomicLong(0L);

    public long get() {
      return value.get();
    }

    public long incrementAndGet() {
      for (;;) {
        long current = get();
        long next = current + 1;
        if (compareAndSet(current, next))
          return next;
      }
    }
    
    public boolean compareAndSet(final long current, final long next) {
      if (value.compareAndSet(current, next)) {
        return true;
      } else {
        LockSupport.parkNanos(1);
        return false;
      }
    }
  }

}
