package io.vertx.reactor;

import reactor.core.CoreSubscriber;

import java.util.function.Consumer;

public interface WriteStreamSubscriber<R> extends CoreSubscriber<R> {
  /**
   * Sets the handler to invoke if the {@link reactor.core.publisher.Flux} that was subscribed to terminates with an error.
   * <p>
   * The underlying {@link io.vertx.core.streams.WriteStream#end()} method is <strong>not</strong> invoked in this case.
   *
   * @return a reference to this, so the API can be used fluently
   */
  WriteStreamSubscriber<R> onError(Consumer<? super Throwable> handler);

  /**
   * Sets the handler to invoke if the {@link reactor.core.publisher.Flux} that was subscribed to terminates successfully.
   * <p>
   * The underlying {@link io.vertx.core.streams.WriteStream#end()} method is invoked <strong>before</strong> the given {@code handler}.
   *
   * @return a reference to this, so the API can be used fluently
   */
  WriteStreamSubscriber<R> onComplete(Runnable handler);

  /**
   * Sets the handler to invoke if the adapted {@link io.vertx.core.streams.WriteStream} fails.
   * <p>
   * The underlying {@link io.vertx.core.streams.WriteStream#end()} method is <strong>not</strong> invoked in this case.
   *
   * @return a reference to this, so the API can be used fluently
   */
  WriteStreamSubscriber<R> onWriteStreamError(Consumer<? super Throwable> handler);
}
