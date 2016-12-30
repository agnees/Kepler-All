package com.kepler.async;

/**
 * Created by 张皆浩 on 2016/12/26.
 * DIDI CORPORATION
 */
public class Promise<T> {

    private final ChainedFutureImpl<T> future;

    public Promise() {
        this.future = new ChainedFutureImpl<>();
    }

    public ChainedFuture<T> newFuture() {
        return this.future;
    }

    public void complete(T value) {
        this.future.complete(value);
    }

    public void complete(Throwable ex) {
        this.future.complete(ex);
    }

}
