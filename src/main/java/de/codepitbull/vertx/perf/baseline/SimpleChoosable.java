package de.codepitbull.vertx.perf.baseline;

import io.vertx.core.spi.cluster.ChoosableIterable;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Iterator;

public class SimpleChoosable<T> implements ChoosableIterable<T> {

  private final T obj;

  public SimpleChoosable(T obj) {
    this.obj = obj;
  }

  public boolean isEmpty() {
    return false;
  }

  @Override
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException();
  }

  public T choose() {
    return obj;
  }
}