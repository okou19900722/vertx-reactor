package io.vertx.reactor.test.gen;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class AsyncResultChecker {

  public int count = 0;

  public <E> Handler<E> expectedResult(E expected) {
    return resultHandler(actual -> assertEquals(expected, actual));
  }

  public <E> Handler<E> resultHandler(Handler<E> expected) {
    return event -> {
      expected.handle(event);
      count++;
    };
  }

  public <E> Handler<AsyncResult<E>> asyncExpectedResult(E expected) {
    return this.asyncResultHandler(e -> assertEquals(expected, e));
  }

  public <R> Handler<AsyncResult<R>> asyncResultHandler(Handler<R> f) {
    return event -> {
      assertTrue(event.succeeded());
      assertFalse(event.failed());
      f.handle(event.result());
      count++;
    };
  }

  public <R> Handler<AsyncResult<R>> failureAsserter(String expectedMsg) {
    return event -> assertAsyncFailure(expectedMsg, event);
  }

  public <T> void assertAsyncFailure(String expectedMsg, AsyncResult<T> result) {
    assertNull(result.result());
    assertFalse(result.succeeded());
    assertTrue(result.failed());
    assertEquals(expectedMsg, result.cause().getMessage());
    count++;
  }
}
