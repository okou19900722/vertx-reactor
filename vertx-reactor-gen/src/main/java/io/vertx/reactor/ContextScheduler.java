package io.vertx.reactor;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.impl.WorkerExecutorInternal;
import io.vertx.core.json.JsonObject;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContextScheduler implements Scheduler {
  private final Vertx vertx;
  private final boolean blocking;
  private final boolean ordered;
  private final Context context;
  private final WorkerExecutor workerExecutor;

  public ContextScheduler(Context context, boolean blocking) {
    this(context, blocking, true);
  }

  public ContextScheduler(Context context, boolean blocking, boolean ordered) {
    this.vertx = context.owner();
    this.context = context;
    this.blocking = blocking;
    this.ordered = ordered;
    this.workerExecutor = null;
  }

  public ContextScheduler(Vertx vertx, boolean blocking) {
    this(vertx, blocking, true);
  }

  public ContextScheduler(Vertx vertx, boolean blocking, boolean ordered) {
    this.vertx = vertx;
    this.context = null;
    this.blocking = blocking;
    this.ordered = ordered;
    this.workerExecutor = null;
  }

  public ContextScheduler(WorkerExecutor workerExecutor) {
    this(workerExecutor, true);
  }

  public ContextScheduler(WorkerExecutor workerExecutor, boolean ordered) {
    Objects.requireNonNull(workerExecutor, "workerExecutor is null");
    this.vertx = ((WorkerExecutorInternal) workerExecutor).vertx();
    this.context = null;
    this.workerExecutor = workerExecutor;
    this.blocking = true;
    this.ordered = ordered;
  }

  @Override
  public Disposable schedule(Runnable task) {
    context.owner().setTimer(1, r -> {

    });
    return null;
  }

  @Override
  public Worker createWorker() {
    return new ContextWorker();
  }

  private static final Object DUMB = new JsonObject();

  public class ContextWorker implements Worker {


    @Override
    public Disposable schedule(Runnable task) {
      return null;
    }

    @Override
    public void dispose() {

    }
  }
}
