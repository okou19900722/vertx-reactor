package io.vertx.reactor.impl;

import io.vertx.core.streams.ReadStream;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public class ReadStreamSubscriber {
  public static <R, J> ReadStream<J> asReadStream(Flux<R> flowable, Function<R, J> adapter) {
    //TODO
    return null;
  }

}
