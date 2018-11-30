package io.vertx.reactor.impl;

import io.vertx.core.streams.ReadStream;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * copy from https://github.com/vert-x3/vertx-rx/blob/63aa157ede46353d83443143dd78ab0a59a7c5e5/rx-java2-gen/src/main/java/io/vertx/reactivex/impl/FlowableReadStream.java
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class FluxReadStream<T, U> extends Flux<U> {
  private final ReadStream<T> stream;
  private final Function<T, U> f;
  private final AtomicReference<Subscription> current;

  public FluxReadStream(ReadStream<T> stream, Function<T, U> f) {
    this.stream = stream;
    this.f = f;
    this.current = new AtomicReference<>();
  }

  private void release() {
    Subscription sub = current.get();
    if (sub != null) {
      if (current.compareAndSet(sub, null)) {
        try {
          stream.exceptionHandler(null);
          stream.endHandler(null);
          stream.handler(null);
        } catch (Exception ignore) {
        } finally {
          stream.resume();
        }
      }
    }
  }

  @Override
  public void subscribe(CoreSubscriber<? super U> actual) {
    Subscription sub = new Subscription() {
      @Override
      public void request(long l) {
        if (current.get() == this) {
          stream.fetch(l);
        }
      }

      @Override
      public void cancel() {
        release();
      }
    };
    if (!current.compareAndSet(null, sub)) {
      actual.onSubscribe(Operators.cancelledSubscription());
      actual.onError(new IllegalStateException("This processor allows only a single Subscriber"));
      return;
    }

    stream.pause();

    stream.endHandler(v -> {
      release();
      actual.onComplete();
    });
    stream.exceptionHandler(err -> {
      release();
      actual.onError(err);
    });
    stream.handler(item -> actual.onNext(f.apply(item)));
    actual.onSubscribe(sub);
  }
}
