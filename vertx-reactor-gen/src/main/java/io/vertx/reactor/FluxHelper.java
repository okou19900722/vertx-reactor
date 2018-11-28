package io.vertx.reactor;

import io.vertx.core.streams.ReadStream;
import io.vertx.reactor.impl.FluxReadStream;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public class FluxHelper {

  public static <T> Flux<T> toFlux(ReadStream<T> stream) {
    return new FluxReadStream<>(stream, Function.identity());
  }
  public static <T, U> Flux<U> toFlux(ReadStream<T> stream, Function<T, U> mapping) {
    return new FluxReadStream<>(stream, mapping);
  }
}
