package com.kepler.async;

import java.util.concurrent.Executor;

/**
 * Created by 张皆浩 on 2016/12/26.
 * DIDI CORPORATION
 */
class ComposeStage<T, U> extends AbstractStage<T, U> {

    private Function<? super T, ? extends ChainedFuture<U>> func;

    ComposeStage(ChainedFutureImpl<T> src, ChainedFutureImpl<U> dep, Function<? super T, ? extends ChainedFuture<U>> func, Executor executor) {
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
            ChainedFutureImpl<U> nextFuture = (ChainedFutureImpl<U>) this.func.apply(this.src.result.throwable,
                    this.src.result.value);
            Relay relay = new Relay(this.executor, nextFuture, this.dep);
            relay.pushAndExec();
        } catch (Throwable ex) {
            this.dep.result = new ChainedFutureImpl.Result<>(ex);
            this.dep.postFire();
        }
    }

    private class Relay extends AbstractStage<U, U> {

        Relay(Executor executor, ChainedFutureImpl<U> src, ChainedFutureImpl<U> dep) {
            super(executor, src, dep);
        }

        @Override
        protected boolean preCheck() {
            return this.src.result != null;
        }

        @Override
        public void doExec() {
            this.dep.result = this.src.result;
            this.dep.postFire();
        }
    }

}