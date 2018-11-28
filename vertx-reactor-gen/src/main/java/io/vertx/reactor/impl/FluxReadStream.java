package io.vertx.reactor.impl;

import io.vertx.core.streams.ReadStream;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public class FluxReadStream<T, U> extends Flux<U> {
  private final ReadStream<T> stream;
  private final Function<T, U> f;

  public FluxReadStream(ReadStream<T> stream, Function<T, U> f) {
    this.stream = stream;
    this.f = f;
  }

  @Override
  public void subscribe(CoreSubscriber actual) {
    //TODO
  }
}
