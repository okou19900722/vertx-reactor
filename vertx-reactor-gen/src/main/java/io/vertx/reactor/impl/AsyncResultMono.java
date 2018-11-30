package io.vertx.reactor.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class AsyncResultMono<T> extends Mono<T> {

  private final Consumer<Handler<AsyncResult<T>>> subscriptionConsumer;

  public AsyncResultMono(Consumer<Handler<AsyncResult<T>>> subscriptionConsumer) {
    this.subscriptionConsumer = subscriptionConsumer;
  }

  public static <T> Mono<T> toMono(Consumer<Handler<AsyncResult<T>>> subscriptionConsumer) {
//    return onAssembly(new AsyncResultMono<>(subscriptionConsumer));
    return Mono.create(sink -> subscriptionConsumer.accept(r -> {
      if (r.succeeded()) {
        sink.success(r.result());
      } else {
        sink.error(r.cause());
      }
    }));
  }

  @Override
  public void subscribe(CoreSubscriber<? super T> actual) {
    //TODO
  }
}
