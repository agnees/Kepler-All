package com.kepler.async;

import java.util.concurrent.Executor;

/**
 * Created by 张皆浩 on 2016/12/26.
 * DIDI CORPORATION
 */
class BiRelay<T, V> extends AbstractStage<T, Void> {

    private final ChainedFutureImpl<V> src2;

    BiRelay(Executor executor, ChainedFutureImpl<T> src, ChainedFutureImpl<V> src2, ChainedFutureImpl<Void> dep) {
        super(executor, src, dep);
        this.src2 = src2;
    }

    @Override
    protected boolean preCheck() {
        return this.src.result != null && this.src2.result != null;
    }

    @Override
    public void doExec() {
        if (this.src.result.throwable != null) {
            this.dep.result = new ChainedFutureImpl.Result<>(this.src.result.throwable);
        } else if (this.src2.result.throwable != null) {
            this.dep.result = new ChainedFutureImpl.Result<>(this.src2.result.throwable);
        } else {
            this.dep.result = new ChainedFutureImpl.Result<>();
        }
        this.dep.postFire();
    }

    @Override
    public ChainedFuture<Void> pushAndExec() {
        if (!preCheck()) {
            src.pushStage(this);
            src2.pushStage(new CoStage(this));
            exec();
        } else {
            doExec();
        }
        return this.dep;
    }

    private class CoStage extends AbstractStage<T, Void> {

        private final BiRelay<T, V> base;

        CoStage(BiRelay<T, V> base) {
            super(null, base.src, base.dep);
            this.base = base;
        }

        @Override
        public synchronized boolean exec() {
            return this.base.exec();
        }

    }

}
