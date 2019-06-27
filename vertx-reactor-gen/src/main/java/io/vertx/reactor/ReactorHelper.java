package io.vertx.reactor;

import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.reactor.impl.FluxReadStream;
import io.vertx.reactor.impl.WriteStreamSubscriberImpl;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public class ReactorHelper {

  /**
   * Adapts a Vert.x {@link WriteStream} to an RxJava {@link Subscriber}.
   * <p>
   * After subscription, the original {@link WriteStream} handlers should not be used anymore as they will be used by the adapter.
   *
   * @param stream the stream to adapt
   *
   * @return the adapted {@link Subscriber}
   */
  public static <T> WriteStreamSubscriber<T> toSubscriber(WriteStream<T> stream) {
    return toSubscriber(stream, Function.identity());
  }

  /**
   * Like {@link #toSubscriber(WriteStream)}, except the provided {@code mapping} function is applied to each {@link Flux} item.
   */
  public static <R, T> WriteStreamSubscriber<R> toSubscriber(WriteStream<T> stream, Function<R, T> mapping) {
    return new WriteStreamSubscriberImpl<>(stream, mapping);
  }

}
