package com.kepler.async;

import java.util.concurrent.Executor;

/**
 * Created by 张皆浩 on 2016/12/26.
 * DIDI CORPORATION
 */
class ApplyStage<T, U> extends AbstractStage<T, U> {

    private Function<? super T, ? extends U> func;

    ApplyStage(ChainedFutureImpl<T> src, ChainedFutureImpl<U> dep, Function<? super T, ? extends U> func, Executor executor) {
        super(executor, src, dep);
        this.func = func;
    }

    @Override
    protected boolean preCheck() {
        return this.src.result != null;
    }

    @Override
    public void doExec() {
        try {
            final U result = this.func.apply(this.src.result.throwable, this.src.result.value);
            this.dep.result = new ChainedFutureImpl.Result<>(result);
            this.dep.postFire();
        } catch (Throwable ex) {
            this.dep.result = new ChainedFutureImpl.Result<>(ex);
            this.dep.postFire();
        }
    }

}