package com.kepler.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

class ChainedFutureImpl<T> extends StackLoop implements ChainedFuture<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChainedFutureImpl.class);

	private AtomicBoolean hasResult = new AtomicBoolean(false);

	private final Object waitLock = new Object();

	static class Result<T> {
		T value;
		Throwable throwable;

		Result() {}

		Result(Throwable throwable) {
			this.throwable = throwable;
		}

		Result(T value) {
			this.value = value;
		}
	}

	volatile Result<T> result;

	private static class WaitStage extends AbstractStage {

		private final Object lock;

		@SuppressWarnings(value = {"unchecked"})
		WaitStage(Object lock) {
			super(null, null, null);
			this.lock = lock;
		}

		@Override
		public synchronized boolean exec() {
			synchronized (lock) {
				this.lock.notifyAll();
			}
			return true;
		}
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (result != null) {
			return false;
		}
		this.complete(new CancellationException());
		return isCancelled();
	}

	@Override
	public boolean isCancelled() {
		return (result != null) && (result.throwable instanceof CancellationException);
	}

	@Override
	public boolean isDone() {
		return this.result != null;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		if (this.result != null) {
			return getOrThrow(this.result);
		}
		timeWait(0);
		return getOrThrow(this.result);
	}

	private T getOrThrow(Result<T> result) throws ExecutionException, InterruptedException {
		if (result.throwable != null) {
            throw new ExecutionException(result.throwable);
        } else {
			return result.value;
        }
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (this.result != null) {
			return getOrThrow(this.result);
		}
		long nano = unit.toNanos(timeout);
		if (!timeWait(nano + System.nanoTime())) {
			throw new TimeoutException();
		}
		return getOrThrow(this.result);
	}

	private boolean timeWait(long dealine) throws InterruptedException, ExecutionException {
		WaitStage stage = new WaitStage(waitLock);
		synchronized (waitLock) {
			pushStage(stage);
			if (dealine <= 0) {
				while (this.result == null) {
					waitLock.wait();
				}
			} else {
				long nano = 0;
				while (this.result == null && ((nano = dealine - System.nanoTime()) > 0)) {
					waitLock.wait(nano);
				}
				if (nano <= 0 && this.result == null) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public <U> ChainedFuture<U> then(Function<? super T, ? extends ChainedFuture<U>> func, Executor executor) {
		return new ComposeStage<>(this, ChainedFutureImpl.<U>newDelegate(), func, executor).pushAndExec();
	}

	@Override
	public <U> ChainedFuture<U> then(Function<? super T, ? extends ChainedFuture<U>> func) {
		return then(func, null);
	}

	@Override
	public <U> ChainedFuture<Void> join(ChainedFuture<U> other) {
		return new BiRelay<>(null, this, (ChainedFutureImpl<U>) other, ChainedFutureImpl.<Void>newDelegate())
				.pushAndExec();
	}

	@Override
	public <U> ChainedFuture<U> apply(Function<? super T, ? extends U> func, Executor executor) {
		return new ApplyStage<>(this, ChainedFutureImpl.<U>newDelegate(), func, executor).pushAndExec();
	}

	@Override
	public <U> ChainedFuture<U> apply(Function<? super T, ? extends U> func) {
		return apply(func, null);
	}

//	@Override
//	public ChainedFuture<T> exceptionally(Function<Throwable, Void> f) {
//		return exceptionally(f, null);
//	}
//
//	@Override
//	public ChainedFuture<T> exceptionally(Function<Throwable, Void> f, Executor executor) {
//		return new ExceptionRelay<>(executor, this, ChainedFutureImpl.<T>newDelegate(), f).pushAndExec();
//	}

	private static <T> ChainedFutureImpl<T> newDelegate() {
		return new ChainedFutureImpl<>();
	}

	@Override
	public void complete(T value) {
		synchronized (waitLock) {
			if (hasResult.compareAndSet(false, true)) {
				this.result = new Result<>(value);
				this.postFire();
			}
		}
	}

	@Override
	public void complete(Throwable value) {
		synchronized (waitLock) {
			if (hasResult.compareAndSet(false, true)) {
				this.result = new Result<>(value);
				this.postFire();
			}
		}
	}

}
