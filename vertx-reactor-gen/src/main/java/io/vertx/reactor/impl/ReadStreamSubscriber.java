package io.vertx.reactor.impl;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.ArrayDeque;
import java.util.function.Function;
/**
 * copy from https://github.com/vert-x3/vertx-rx/blob/63aa157ede46353d83443143dd78ab0a59a7c5e5/rx-java2-gen/src/main/java/io/vertx/reactivex/impl/ReadStreamSubscriber.java
 *
 * An RxJava {@code Subscriber} that turns an {@code Observable} into a {@link ReadStream}.
 * <p>
 * The stream implements the {@link #pause()} and {@link #resume()} operation by maintaining
 * a buffer of {@link #BUFFER_SIZE} elements between the {@code Observable} and the {@code ReadStream}.
 * <p>
 * When the subscriber is created it requests {@code 0} elements to activate the subscriber's back-pressure.
 * Setting the handler initially on the {@code ReadStream} triggers a request of {@link #BUFFER_SIZE} elements.
 * When the item buffer is half empty, new elements are requested to fill the buffer back to {@link #BUFFER_SIZE}
 * elements.
 * <p>
 * The {@link #endHandler(Handler<Void>)} is called when the {@code Observable} is completed or has failed and
 * no pending elements, emitted before the completion or failure, are still in the buffer, i.e the handler
 * is not called when the stream is paused.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ReadStreamSubscriber<R, J> implements Subscriber<R>, ReadStream<J> {

  private static final Runnable NOOP_ACTION = () -> {};
  private static final Throwable DONE_SENTINEL = new Throwable();

  public static final int BUFFER_SIZE = 16;

  public static <R, J> ReadStream<J> asReadStream(Flux<R> flowable, Function<R, J> adapter) {
    ReadStreamSubscriber<R, J> observer = new ReadStreamSubscriber<>(adapter);
    flowable.subscribe(observer);
    return observer;
  }


  private final Function<R, J> adapter;
  private Handler<Void> endHandler;
  private Handler<Throwable> exceptionHandler;
  private Handler<J> elementHandler;
  private boolean paused = false;
  private Throwable completed;
  private ArrayDeque<R> pending = new ArrayDeque<>();
  private int requested = 0;
  private Subscription subscription;

  public ReadStreamSubscriber(Function<R, J> adapter) {
    this.adapter = adapter;
  }

  @Override
  public ReadStream<J> handler(Handler<J> handler) {
    synchronized (this) {
      elementHandler = handler;
    }
    checkStatus();
    return this;
  }

  @Override
  public ReadStream<J> pause() {
    synchronized (this) {
      paused = true;
    }
    return this;
  }

  @Override
  public ReadStream<J> fetch(long amount) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public ReadStream<J> resume() {
    synchronized (this) {
      paused = false;
    }
    checkStatus();
    return this;
  }

  @Override
  public void onSubscribe(Subscription s) {
    synchronized (this) {
      subscription = s;
    }
    checkStatus();
  }

  private void checkStatus() {
    Runnable action = NOOP_ACTION;
    while (true) {
      J adapted;
      Handler<J> handler;
      synchronized (this) {
        if (!paused && (handler = elementHandler) != null && pending.size() > 0) {
          requested--;
          R item = pending.poll();
          adapted = adapter.apply(item);
        } else {
          if (completed != null) {
            if (pending.isEmpty()) {
              Handler<Throwable> onError;
              Throwable result;
              if (completed != DONE_SENTINEL) {
                onError = exceptionHandler;
                result = completed;
                exceptionHandler = null;
              } else {
                onError = null;
                result = null;
              }
              Handler<Void> onCompleted = endHandler;
              endHandler = null;
              action = () -> {
                try {
                  if (onError != null) {
                    onError.handle(result);
                  }
                } finally {
                  if (onCompleted != null) {
                    onCompleted.handle(null);
                  }
                }
              };
            }
          } else if (elementHandler != null && requested < BUFFER_SIZE / 2) {
            int request = BUFFER_SIZE - requested;
            action = () -> subscription.request(request);
            requested = BUFFER_SIZE;
          }
          break;
        }
      }
      handler.handle(adapted);
    }
    action.run();
  }

  @Override
  public ReadStream<J> endHandler(Handler<Void> handler) {
    synchronized (this) {
      if (completed == null || pending.size() > 0) {
        endHandler = handler;
      } else {
        if (handler != null) {
          throw new IllegalStateException();
        }
      }
    }
    return this;
  }

  @Override
  public ReadStream<J> exceptionHandler(Handler<Throwable> handler) {
    synchronized (this) {
      if (completed == null || pending.size() > 0) {
        exceptionHandler = handler;
      } else {
        if (handler != null) {
          throw new IllegalStateException();
        }
      }
    }
    return this;
  }

  @Override
  public void onComplete() {
    onError(DONE_SENTINEL);
  }

  @Override
  public void onError(Throwable e) {
    synchronized (this) {
      if (completed != null) {
        return;
      }
      completed = e;
    }
    checkStatus();
  }

  @Override
  public void onNext(R item) {
    synchronized (this) {
      pending.add(item);
    }
    checkStatus();
  }
}
