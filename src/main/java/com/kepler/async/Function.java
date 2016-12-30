package com.kepler.async;

public interface Function<T, R> {

	R apply(Throwable err, T param);

}
