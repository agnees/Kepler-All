package com.kepler.async;

import java.util.concurrent.Executor;

/**
 * Created by 张皆浩 on 2016/12/26.
 * DIDI CORPORATION
 */
class ExceptionRelay<T> extends AbstractStage<T, T> {

    private final Function<Throwable, Void> func;

    ExceptionRelay(Executor executor, ChainedFutureImpl<T> src, ChainedFutureImpl<T> dep, Function<Throwable, Void> func) {
        super(executor, src, dep);
        this.func = func;
    }

    @Override
    protected boolean preCheck() {
        return this.src.result != null;
    }

    @Override
    public void doExec() {
        if (this.src.result.throwable != null) {
            try {
                this.func.apply(this.src.result.throwable, null);
                this.dep.result = new ChainedFutureImpl.Result<T>();
            } catch (Throwable ex) {
                this.dep.result = new ChainedFutureImpl.Result<>(ex);
            }
            this.dep.postFire();
        } else {
            this.dep.result = new ChainedFutureImpl.Result<>(this.src.result.value);
            this.dep.postFire();
        }
    }
}