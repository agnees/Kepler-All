package com.kepler.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 张皆浩 on 2016/12/28.
 * DIDI CORPORATION
 */
public class Futures {

    private static final Logger LOGGER = LoggerFactory.getLogger(Futures.class);

    public static <T> ChainedFuture<T> newAsync(final Function0<T> func) {
        final Promise<T> promise = new Promise<>();
        final ChainedFuture<T> future = promise.newFuture();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final T result = func.apply();
                    promise.complete(result);
                } catch (Throwable t) {
                    promise.complete(t);
                }
            }
        }).start();
        return future;
    }

}
