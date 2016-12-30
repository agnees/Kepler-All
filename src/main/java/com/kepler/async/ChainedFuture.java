package com.kepler.async;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface ChainedFuture<T> extends Future<T> {

	<U> ChainedFuture<U> then(Function<? super T, ? extends ChainedFuture<U>> func, Executor executor);

	<U> ChainedFuture<U> then(Function<? super T, ? extends ChainedFuture<U>> func);

	<U> ChainedFuture<Void> join(ChainedFuture<U> b);

	<U> ChainedFuture<U> apply(Function<? super T, ? extends U> func, Executor executor);

	<U> ChainedFuture<U> apply(Function<? super T, ? extends U> func);
//
//	ChainedFuture<T> exceptionally(Function<Throwable, Void> f);
//
//	ChainedFuture<T> exceptionally(Function<Throwable, Void> f, Executor executor);

	void complete(T value);
	
	void complete(Throwable value);
}
