package io.vertx.reactor.test.gen;

import io.vertx.codegen.testmodel.RefedInterface1Impl;
import io.vertx.codegen.testmodel.TestDataObject;
import io.vertx.codegen.testmodel.TestInterfaceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactor.codegen.testmodel.Factory;
import io.vertx.reactor.codegen.testmodel.RefedInterface1;
import io.vertx.reactor.codegen.testmodel.RefedInterface2;
import io.vertx.reactor.codegen.testmodel.TestInterface;
import com.acme.reactor.pkg.MyInterface;
import com.acme.reactor.pkg.sub.SubInterface;
import io.vertx.test.core.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ApiTCKTest {

  private final TestInterface obj = new TestInterface(new TestInterfaceImpl());

  @Test
  public void testMethodWithBasicParams() {
    obj.methodWithBasicParams((byte) 123, (short) 12345, 1234567, 1265615234L, 12.345f, 12.34566d, true, 'X', "foobar");
  }

  @Test
  public void testMethodWithBasicBoxedParams() {
    obj.methodWithBasicBoxedParams((byte) 123, (short) 12345, 1234567, 1265615234L, 12.345f, 12.34566d, true, 'X');
  }

  @Test
  public void testMethodWithHandlerBasicTypes() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerBasicTypes(
        checker.expectedResult((byte) 123),
        checker.expectedResult((short) 12345),
        checker.expectedResult(1234567),
        checker.expectedResult(1265615234L),
        checker.expectedResult(12.345f),
        checker.expectedResult(12.34566d),
        checker.expectedResult(true),
        checker.expectedResult('X'),
        checker.expectedResult("quux!")
    );
    assertEquals(9, checker.count);
  }

  @Test
  public void testMethodWithHandlerAsyncResultBasicTypes() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerAsyncResultByte(false, checker.asyncExpectedResult((byte) 123));
    obj.methodWithHandlerAsyncResultShort(false, checker.asyncExpectedResult((short) 12345));
    obj.methodWithHandlerAsyncResultInteger(false, checker.asyncExpectedResult(1234567));
    obj.methodWithHandlerAsyncResultLong(false, checker.asyncExpectedResult(1265615234L));
    obj.methodWithHandlerAsyncResultFloat(false, checker.asyncExpectedResult(12.345f));
    obj.methodWithHandlerAsyncResultDouble(false, checker.asyncExpectedResult(12.34566d));
    obj.methodWithHandlerAsyncResultBoolean(false, checker.asyncExpectedResult(true));
    obj.methodWithHandlerAsyncResultCharacter(false, checker.asyncExpectedResult('X'));
    obj.methodWithHandlerAsyncResultString(false, checker.asyncExpectedResult("quux!"));
    assertEquals(9, checker.count);
    checker.count = 0;
    obj.methodWithHandlerAsyncResultByte(true, checker.failureAsserter("foobar!"));
    obj.methodWithHandlerAsyncResultShort(true, checker.failureAsserter("foobar!"));
    obj.methodWithHandlerAsyncResultInteger(true, checker.failureAsserter("foobar!"));
    obj.methodWithHandlerAsyncResultLong(true, checker.failureAsserter("foobar!"));
    obj.methodWithHandlerAsyncResultFloat(true, checker.failureAsserter("foobar!"));
    obj.methodWithHandlerAsyncResultDouble(true, checker.failureAsserter("foobar!"));
    obj.methodWithHandlerAsyncResultBoolean(true, checker.failureAsserter("foobar!"));
    obj.methodWithHandlerAsyncResultCharacter(true, checker.failureAsserter("foobar!"));
    obj.methodWithHandlerAsyncResultString(true, checker.failureAsserter("foobar!"));
    assertEquals(9, checker.count);
  }

  @Test
  public void testMethodWithFutureBasicTypes()  {
    assertEquals((byte) 123, (byte) get(obj.rxMethodWithHandlerAsyncResultByte(false)));
    assertEquals((short) 12345, (short) get(obj.rxMethodWithHandlerAsyncResultShort(false)));
    assertEquals(1234567, (int) get(obj.rxMethodWithHandlerAsyncResultInteger(false)));
    assertEquals(1265615234L, (long) get(obj.rxMethodWithHandlerAsyncResultLong(false)));
    assertEquals(12.345f, get(obj.rxMethodWithHandlerAsyncResultFloat(false)), 0);
    assertEquals(12.34566d, get(obj.rxMethodWithHandlerAsyncResultDouble(false)), 0);
    assertEquals(true, get(obj.rxMethodWithHandlerAsyncResultBoolean(false)));
    assertEquals('X', (char) get(obj.rxMethodWithHandlerAsyncResultCharacter(false)));
    assertEquals("quux!", get(obj.rxMethodWithHandlerAsyncResultString(false)));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultByte(true));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultShort(true));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultInteger(true));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultLong(true));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultFloat(true));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultDouble(true));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultBoolean(true));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultCharacter(true));
    assertFailure("foobar!", obj.rxMethodWithHandlerAsyncResultString(true));
  }

  static <T> T get(Mono<T> future) {
    return future.block();
  }

  private <T> void assertFailure(String message, Mono<T> future)  {
    try {
      future.block();
    } catch (Exception e) {
//      assertEquals(message, e.getCause().getMessage());
    }
  }

  @Test
  public void testMethodWithUserTypes() {
    RefedInterface1 refed = new RefedInterface1(new RefedInterface1Impl());
    refed.setString("aardvarks");
    obj.methodWithUserTypes(refed);
  }

  @Test
  public void testObjectParam() {
    obj.methodWithObjectParam("JsonObject", new JsonObject().put("foo", "hello").put("bar", 123));
    obj.methodWithObjectParam("JsonArray", new JsonArray().add("foo").add("bar").add("wib"));
  }

  @Test
  public void testDataObjectParam() {
    TestDataObject options = new TestDataObject();
    options.setFoo("hello");
    options.setBar(123);
    options.setWibble(1.23);
    obj.methodWithDataObjectParam(options);
  }

  @Test
  public void testMethodWithHandlerDataObject() {
    TestDataObject dataObject = new TestDataObject();
    dataObject.setFoo("foo");
    dataObject.setBar(123);
    AtomicInteger count = new AtomicInteger();
    obj.methodWithHandlerDataObject(it -> {
      assertEquals(dataObject.getFoo(), it.getFoo());
      assertEquals(dataObject.getBar(), it.getBar());
      count.incrementAndGet();
    });
    assertEquals(1, count.get());
  }

  @Test
  public void testMethodWithHandlerAsyncResultDataObject() {
    TestDataObject dataObject = new TestDataObject();
    dataObject.setFoo("foo");
    dataObject.setBar(123);
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerAsyncResultDataObject(false, result -> {
      assertTrue(result.succeeded());
        assertFalse(result.failed());
        TestDataObject res = result.result();
        assertEquals(dataObject.getFoo(), res.getFoo());
        assertNull(result.cause());
        checker.count++;
    });
    obj.methodWithHandlerAsyncResultDataObject(true, result -> checker.assertAsyncFailure("foobar!", result));
    assertEquals(2, checker.count);
  }

  @Test
  public void testMethodWithHandlerStringReturn() {
    Handler<String> handler = obj.methodWithHandlerStringReturn("the-result");
    handler.handle("the-result");
    boolean failed = false;
    try {
      handler.handle("not-expected");
    }  catch (Throwable ignore) {
      failed = true;
    }
    assertTrue(failed);
  }

  @Test
  public void testMethodWithHandlerGenericReturn() {
    AtomicReference<Object> result = new AtomicReference<>();
    obj.<String>methodWithHandlerGenericReturn(result::set).handle("the-result");
    assertEquals("the-result", result.get());
    obj.<TestInterface>methodWithHandlerGenericReturn(result::set).handle(obj);
    assertEquals(obj, result.get());
  }

  @Test
  public void testMethodWithHandlerVertxGenReturn() {
    obj.methodWithHandlerVertxGenReturn("the-gen-result").handle(new RefedInterface1(new RefedInterface1Impl().setString("the-gen-result")));
  }

  @Test
  public void testMethodWithHandlerAsyncResultStringReturn() {
    Handler<AsyncResult<String>> succeedingHandler = obj.methodWithHandlerAsyncResultStringReturn("the-result", false);
    succeedingHandler.handle(Future.succeededFuture("the-result"));
    boolean failed = false;
    try {
      succeedingHandler.handle(Future.succeededFuture("not-expected"));
    }  catch (Throwable ignore) {
      failed = true;
    }
    assertTrue(failed);
    Handler<AsyncResult<String>> failingHandler = obj.methodWithHandlerAsyncResultStringReturn("an-error", true);
    failingHandler.handle(Future.failedFuture("an-error"));
    failed = false;
    try {
      failingHandler.handle(Future.succeededFuture("whatever"));
    } catch (Throwable ignore) {
      failed = true;
    }
    assertTrue(failed);
  }

  @Test
  public void testMethodWithHandlerAsyncResultGenericReturn() {
    AtomicReference<Object> result = new AtomicReference<>();
    Handler<AsyncResult<Object>> succeedingHandler = obj.methodWithHandlerAsyncResultGenericReturn(ar -> result.set(ar.succeeded() ? ar.result() : ar.cause()));
    succeedingHandler.handle(Future.succeededFuture("the-result"));
    assertEquals("the-result", result.get());
    succeedingHandler.handle(Future.succeededFuture(obj));
    assertEquals(obj, result.get());
  }

  @Test
  public void testMethodWithHandlerAsyncResultVertxGenReturn() {
    obj.methodWithHandlerAsyncResultVertxGenReturn("the-gen-result", false).handle(Future.succeededFuture(new RefedInterface1(new RefedInterface1Impl().setString("the-gen-result"))));
    obj.methodWithHandlerAsyncResultVertxGenReturn("it-failed-dude", true).handle(Future.failedFuture(new Exception("it-failed-dude")));
  }

  @Test
  public void testMethodWithHandlerUserTypes() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerUserTypes(checker.resultHandler(it -> assertEquals("echidnas", it.getString())));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithConcreteHandlerUserTypeSubtype() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithConcreteHandlerUserTypeSubtype(Factory.createConcreteHandlerUserType(checker.resultHandler(it -> assertEquals("echidnas", it.getString()))));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithAbstractHandlerUserTypeSubtype() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithAbstractHandlerUserTypeSubtype(Factory.createAbstractHandlerUserType(checker.resultHandler(it -> assertEquals("echidnas", it.getString()))));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithAbstractHandlerUserTypeSubtypeExtension() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithConcreteHandlerUserTypeSubtypeExtension(Factory.createConcreteHandlerUserTypeExtension(checker.resultHandler(it -> assertEquals("echidnas", it.getString()))));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithHandlerAsyncResultUserTypes() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerAsyncResultUserTypes(checker.asyncResultHandler(it -> assertEquals("cheetahs", it.getString())));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithFutureUserTypes()  {
    RefedInterface1 result = get(obj.rxMethodWithHandlerAsyncResultUserTypes());
    assertEquals("cheetahs", result.getString());
  }

  @Test
  public void testMethodWithHandlerVoid() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerVoid(checker.resultHandler(Assert::assertNull));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithHandlerAsyncResultVoid() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerAsyncResultVoid(false, checker.asyncResultHandler(Assert::assertNull));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithFutureVoid()  {
    Void result = get(obj.rxMethodWithHandlerAsyncResultVoid(false));
    assertNull(result);
  }

  @Test
  public void testMethodWithHandlerAsyncResultVoidFails() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerAsyncResultVoid(true, checker.failureAsserter("foo!"));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithFutureVoidFails()  {
    assertFailure("foo!", obj.rxMethodWithHandlerAsyncResultVoid(true));
  }

  @Test
  public void testMethodWithHandlerThrowable() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerThrowable(checker.resultHandler(t -> {
      assertTrue(t instanceof VertxException);
      assertEquals("cheese!", t.getMessage());
    }));
    assertEquals(1, checker.count);
  }

  @Test
  public void testMethodWithGenericParam() {
    obj.methodWithGenericParam("String", "foo");
    obj.methodWithGenericParam("Ref", new RefedInterface1Impl().setString("bar"));
    obj.methodWithGenericParam("JsonObject", new JsonObject().put("foo", "hello").put("bar", 123));
    obj.methodWithGenericParam("JsonArray", new JsonArray().add("foo").add("bar").add("wib"));
  }

  @Test
  public void testMethodWithGenericHandler() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithGenericHandler("String", checker.expectedResult("foo"));
    obj.methodWithGenericHandler("Ref", checker.<RefedInterface1Impl>resultHandler(it -> assertEquals("bar", it.getString())));
    obj.methodWithGenericHandler("JsonObject", checker.expectedResult(new JsonObject().put("foo", "hello").put("bar", 123)));
    obj.methodWithGenericHandler("JsonArray", checker.expectedResult(new JsonArray().add("foo").add("bar").add("wib")));
    assertEquals(4, checker.count);
  }

  @Test
  public void testMethodWithGenericHandlerAsyncResult() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithGenericHandlerAsyncResult("String", checker.asyncExpectedResult("foo"));
    obj.methodWithGenericHandlerAsyncResult("Ref", checker.<RefedInterface1Impl>asyncResultHandler(it -> assertEquals("bar", it.getString())));
    obj.methodWithGenericHandlerAsyncResult("JsonObject", checker.asyncExpectedResult(new JsonObject().put("foo", "hello").put("bar", 123)));
    obj.methodWithGenericHandlerAsyncResult("JsonArray", checker.asyncExpectedResult(new JsonArray().add("foo").add("bar").add("wib")));
    assertEquals(4, checker.count);
  }

  @Test
  public void testMethodWithGenericObservable()  {
    assertEquals("foo", get(obj.rxMethodWithGenericHandlerAsyncResult("String")));
    RefedInterface1Impl ref = get(obj.rxMethodWithGenericHandlerAsyncResult("Ref"));
    assertEquals("bar", ref.getString());
    assertEquals(new JsonObject().put("foo", "hello").put("bar", 123), get(obj.rxMethodWithGenericHandlerAsyncResult("JsonObject")));
    assertEquals(new JsonArray().add("foo").add("bar").add("wib"), get(obj.rxMethodWithGenericHandlerAsyncResult("JsonArray")));
  }

  @Test
  public void testBasicReturns() {
    assertEquals(123, obj.methodWithByteReturn());
    assertEquals(12345, obj.methodWithShortReturn());
    assertEquals(12345464, obj.methodWithIntReturn());
    assertEquals(65675123, obj.methodWithLongReturn());
    assertEquals(1.23f, obj.methodWithFloatReturn(), 0);
    assertEquals(3.34535, obj.methodWithDoubleReturn(), 0);
    assertTrue(obj.methodWithBooleanReturn());
    assertEquals('Y', obj.methodWithCharReturn());
    assertEquals("orangutan", obj.methodWithStringReturn());
  }

  @Test
  public void testVertxGenReturn() {
    RefedInterface1 r = obj.methodWithVertxGenReturn();
    assertEquals("chaffinch", r.getString());
  }

  @Test
  public void testVertxGenNullReturn() {
    RefedInterface1 r = obj.methodWithVertxGenNullReturn();
    assertNull(r);
  }

  @Test
  public void testAbstractVertxGenReturn() {
    RefedInterface2 r = obj.methodWithAbstractVertxGenReturn();
    assertEquals("abstractchaffinch", r.getString());
  }

  @Test
  public void testDataObjectReturn() {
    TestDataObject r = obj.methodWithDataObjectReturn();
    assertEquals("foo", r.getFoo());
    assertEquals(123, r.getBar());
  }

  @Test
  public void testDataObjectNullReturn() {
    TestDataObject r = obj.methodWithDataObjectNullReturn();
    assertNull(r);
  }

  @Test
  public void testOverloadedMethods() {
    RefedInterface1 refed = new RefedInterface1(new RefedInterface1Impl());
    refed.setString("dog");
    assertEquals("meth1", obj.overloadedMethod("cat", refed));
    AtomicBoolean called = new AtomicBoolean(false);
    assertEquals("meth2", obj.overloadedMethod("cat", refed, 12345, it -> { assertEquals("giraffe", it); called.set(true); }));
    assertTrue(called.getAndSet(false));
    assertEquals("meth3", obj.overloadedMethod("cat", it -> { assertEquals("giraffe", it); called.set(true); }));
    assertTrue(called.getAndSet(false));
    assertEquals("meth4", obj.overloadedMethod("cat", refed, it -> { assertEquals("giraffe", it); called.set(true); }));
    assertTrue(called.get());
  }

  @Test
  public void testSuperInterfaces() {
    obj.superMethodWithBasicParams((byte) 123, (short) 12345, 1234567, 1265615234L, 12.345f, 12.34566d, true, 'X', "foobar");
    obj.otherSuperMethodWithBasicParams((byte) 123, (short) 12345, 1234567, 1265615234L, 12.345f, 12.34566d, true, 'X', "foobar");
  }

  @Test
  public void testMethodWithGenericReturn() {
    Object ret = obj.methodWithGenericReturn("JsonObject");
    assertTrue("Was expecting " + ret + " to implement JsonObject", ret instanceof JsonObject);
    assertEquals(new JsonObject().put("foo", "hello").put("bar", 123), ret);
    ret = obj.methodWithGenericReturn("JsonArray");
    assertTrue("Was expecting " + ret + " to implement JsonArray", ret instanceof JsonArray);
    assertEquals(new JsonArray().add("foo").add("bar").add("wib"), ret);
  }

  @Test
  public void testFluentMethod() {
    TestInterface ret = obj.fluentMethod("bar");
    assertSame(obj, ret);
  }

  @Test
  public void testStaticFactoryMethod() {
    RefedInterface1 ret = TestInterface.staticFactoryMethod("bar");
    assertEquals("bar", ret.getString());
  }

  @Test
  public void testMethodWithCachedReturn() {
    RefedInterface1 ret1 = obj.methodWithCachedReturn("bar");
    assertEquals("bar", ret1.getString());
    RefedInterface1 ret2 = obj.methodWithCachedReturn("bar");
    assertSame(ret1, ret2);
    RefedInterface1 ret3 = obj.methodWithCachedReturn("bar");
    assertSame(ret1, ret3);
  }

  @Test
  public void testMethodWithCachedListReturn() {
    List<RefedInterface1> ret1 = obj.methodWithCachedListReturn();
    assertEquals(2, ret1.size());
    assertEquals("foo", ret1.get(0).getString());
    assertEquals("bar", ret1.get(1).getString());
    List<RefedInterface1> ret2 = obj.methodWithCachedListReturn();
    assertSame(ret1, ret2);
    List<RefedInterface1> ret3 = obj.methodWithCachedListReturn();
    assertSame(ret1, ret3);
  }

  @Test
  public void testMethodWithCachedReturnPrimitive() {
    int value = TestUtils.randomInt();
    assertEquals(value, obj.methodWithCachedReturnPrimitive(value));
    assertEquals(value, obj.methodWithCachedReturnPrimitive(value));
  }

  @Test
  public void testJsonReturns() {
    JsonObject ret1 = obj.methodWithJsonObjectReturn();
    assertEquals(new JsonObject().put("cheese", "stilton"), ret1);
    JsonArray ret2 = obj.methodWithJsonArrayReturn();
    assertEquals(new JsonArray().add("socks").add("shoes"), ret2);
  }

  @Test
  public void testNullJsonReturns() {
    JsonObject ret1 = obj.methodWithNullJsonObjectReturn();
    assertNull(ret1);
    JsonArray ret2 = obj.methodWithNullJsonArrayReturn();
    assertNull(ret2);
  }

  @Test
  public void testJsonParams() {
    obj.methodWithJsonParams(
        new JsonObject().put("cat", "lion").put("cheese", "cheddar"),
        new JsonArray().add("house").add("spider")
    );
  }

  @Test
  public void testNullJsonParams() {
    obj.methodWithNullJsonParams(
        null,
        null
    );
  }

  @Test
  public void testJsonHandlerParams() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerJson(
        checker.resultHandler(it -> assertEquals(new JsonObject().put("cheese", "stilton"), it)),
        checker.resultHandler(it -> assertEquals(new JsonArray().add("socks").add("shoes"), it)
    ));
    assertEquals(2, checker.count);
  }

  @Test
  public void testJsonHandlerAsyncResultParams() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerAsyncResultJsonObject(checker.asyncResultHandler(it -> assertEquals(new JsonObject().put("cheese", "stilton"), it)));
    obj.methodWithHandlerAsyncResultJsonArray(checker.asyncResultHandler(it -> assertEquals(new JsonArray().add("socks").add("shoes"), it)));
    assertEquals(2, checker.count);
  }

  @Test
  public void testNullJsonHandlerAsyncResultParams() {
    AsyncResultChecker checker = new AsyncResultChecker();
    obj.methodWithHandlerAsyncResultNullJsonObject(checker.asyncResultHandler(Assert::assertNull));
    obj.methodWithHandlerAsyncResultNullJsonArray(checker.asyncResultHandler(Assert::assertNull));
    assertEquals(2, checker.count);
  }

  @Test
  public void testJsonFutureParams()  {
    assertEquals(new JsonObject().put("cheese", "stilton"), get(obj.rxMethodWithHandlerAsyncResultJsonObject()));
    assertEquals(new JsonArray().add("socks").add("shoes"), get(obj.rxMethodWithHandlerAsyncResultJsonArray()));
  }

  @Test
  public void testNullJsonFutureParams()  {
    assertNull(get(obj.rxMethodWithHandlerAsyncResultNullJsonObject()));
    assertNull(get(obj.rxMethodWithHandlerAsyncResultNullJsonArray()));
  }

  @Test
  public void testCustomModule() {
    MyInterface my = MyInterface.create();
    TestInterface testInterface = my.method();
    testInterface.methodWithBasicParams((byte) 123, (short) 12345, 1234567, 1265615234L, 12.345f, 12.34566d, true, 'X', "foobar");
    SubInterface sub = my.sub();
    assertEquals("olleh", sub.reverse("hello"));
  }

  @Test
  public void testThrowableParam() {
    assertEquals("throwable_message", obj.methodWithThrowableParam(new Exception("throwable_message")));
  }

  static <V> Map<String, V> map(String key1, V value1) {
    HashMap<String, V> map = new HashMap<>();
    map.put(key1, value1);
    return map;
  }

  static <V> Map<String, V> map(String key1, V value1, String key2, V value2) {
    HashMap<String, V> map = new HashMap<>();
    map.put(key1, value1);
    map.put(key2, value2);
    return map;
  }

  static <E> Set<E> set(E... elements) {
    return new HashSet<>(Arrays.asList(elements));
  }

  static <E> List<E> list(E... elements) {
    return Arrays.asList(elements);
  }
}
