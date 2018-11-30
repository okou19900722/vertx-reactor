package io.vertx.reactor.test;

import io.vertx.lang.rx.test.ReadStreamSubscriberTestBase;
import io.vertx.reactor.impl.ReadStreamSubscriber;
import org.reactivestreams.Subscription;

import java.util.function.Function;


public class ReadStreamSubscriberTest extends ReadStreamSubscriberTestBase {
  @Override
  public long bufferSize() {
    return ReadStreamSubscriber.BUFFER_SIZE;
  }

  @Override
  protected Sender sender() {
    return new Sender() {

      private ReadStreamSubscriber<String, String> subscriber = new ReadStreamSubscriber<>(Function.identity());

      {
        stream = subscriber;
        subscriber.onSubscribe(new Subscription() {
          @Override
          public void request(long n) {
            requested += n;
          }

          @Override
          public void cancel() {
          }
        });
      }

      protected void emit() {
        subscriber.onNext("" + seq++);
      }

      protected void complete() {
        subscriber.onComplete();
      }

      protected void fail(Throwable cause) {
        subscriber.onError(cause);
      }

    };
  }
}
