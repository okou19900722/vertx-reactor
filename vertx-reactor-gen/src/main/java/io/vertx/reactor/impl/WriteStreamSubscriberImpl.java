package io.vertx.reactor.impl;

import io.vertx.core.streams.WriteStream;
import io.vertx.reactor.WriteStreamSubscriber;
import org.reactivestreams.Subscription;
import reactor.core.Exceptions;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class WriteStreamSubscriberImpl<R, T> implements WriteStreamSubscriber<R> {

  private static final int BATCH_SIZE = 16;

  private final WriteStream<T> writeStream;
  private final Function<R, T> mapping;

  private Subscription subscription;
  private int outstanding;
  private boolean done;

  private Consumer<? super Throwable> flowableErrorHandler;
  private Runnable flowableCompleteHandler;
  private Consumer<? super Throwable> writeStreamExceptionHandler;

  public WriteStreamSubscriberImpl(WriteStream<T> writeStream, Function<R, T> mapping) {
    Objects.requireNonNull(writeStream, "writeStream");
    Objects.requireNonNull(mapping, "mapping");
    this.writeStream = writeStream;
    this.mapping = mapping;
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    Objects.requireNonNull(subscription, "subscription");
    if (!setSubscription(subscription)) {
      subscription.cancel();
      Operators.reportSubscriptionSet();
      return;
    }
    writeStream.exceptionHandler(t -> {
      if (!setDone()) {
        Operators.onErrorDropped(t, Context.empty());
        return;
      }
      getSubscription().cancel();
      Consumer<? super Throwable> c;
      synchronized (this) {
        c = this.writeStreamExceptionHandler;
      }
      if (c != null) {
        try {
          c.accept(t);
        } catch (Exception e) {
          Operators.onErrorDropped(e, Context.empty());
        }
      }
    });
    writeStream.drainHandler(v -> requestMore());
    requestMore();
  }

  @Override
  public void onNext(R r) {
    if (isDone()) {
      return;
    }

    if (r == null) {
      Throwable throwable = new NullPointerException("onNext called with null");
      try {
        getSubscription().cancel();
      } catch (Throwable t) {
        Exceptions.throwIfFatal(t);
        throwable = Exceptions.multiple(throwable, t);
      }
      onError(throwable);
      return;
    }

    try {
      writeStream.write(mapping.apply(r));
      synchronized (this) {
        outstanding--;
      }
    } catch (Throwable t) {
      Exceptions.throwIfFatal(t);
      Throwable throwable;
      try {
        getSubscription().cancel();
        throwable = t;
      } catch (Throwable t1) {
        Exceptions.throwIfFatal(t1);
        throwable = Exceptions.multiple(t, t1);
      }
      onError(throwable);
      return;
    }

    if (!writeStream.writeQueueFull()) {
      requestMore();
    }
  }

  @Override
  public void onError(Throwable t) {
    if (!setDone()) {
      Operators.onErrorDropped(t, Context.empty());
      return;
    }

    Objects.requireNonNull(t, "onError called with null");

    Consumer<? super Throwable> c;
    synchronized (this) {
      c = flowableErrorHandler;
    }
    try {
      if (c != null) {
        c.accept(t);
      }
    } catch (Throwable t1) {
      Exceptions.throwIfFatal(t1);
      Operators.onErrorDropped(t1, Context.empty());
    }
  }

  @Override
  public void onComplete() {
    if (!setDone()) {
      return;
    }

    Runnable a;
    synchronized (this) {
      a = flowableCompleteHandler;
    }
    try {
      writeStream.end();
      if (a != null) {
        a.run();
      }
    } catch (Throwable t) {
      Exceptions.throwIfFatal(t);
      Operators.onErrorDropped(t, Context.empty());
    }
  }

  private synchronized Subscription getSubscription() {
    return subscription;
  }

  private synchronized boolean setSubscription(Subscription subscription) {
    if (this.subscription == null) {
      this.subscription = subscription;
      return true;
    }
    return false;
  }

  private synchronized boolean isDone() {
    return done;
  }

  private synchronized boolean setDone() {
    return done ? false : (done = true);
  }

  private void requestMore() {
    Subscription s = getSubscription();
    if (s == null) {
      return;
    }
    synchronized (this) {
      if (done || outstanding > 0) {
        return;
      }
      outstanding = BATCH_SIZE;
    }
    s.request(BATCH_SIZE);
  }

  @Override
  public synchronized WriteStreamSubscriber<R> onError(Consumer<? super Throwable> handler) {
    this.flowableErrorHandler = handler;
    return this;
  }

  @Override
  public synchronized WriteStreamSubscriber<R> onComplete(Runnable handler) {
    this.flowableCompleteHandler = handler;
    return this;
  }

  @Override
  public synchronized WriteStreamSubscriber<R> onWriteStreamError(Consumer<? super Throwable> handler) {
    this.writeStreamExceptionHandler = handler;
    return this;
  }
}
