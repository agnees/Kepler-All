package com.kepler.async;

import java.util.concurrent.Executor;

/**
 * Created by 张皆浩 on 2016/12/26.
 * DIDI CORPORATION
 */
abstract class AbstractStage<T, U> implements Runnable {
    Executor executor;
    ChainedFutureImpl<T> src;
    ChainedFutureImpl<U> dep;
    AbstractStage(Executor executor, ChainedFutureImpl<T> src, ChainedFutureImpl<U> dep) {
        this.src = src;
        this.dep = dep;
        this.executor = executor;
    }

    protected boolean preCheck() {
        return false;
    }

    protected void doExec() {}

    public ChainedFuture<U> pushAndExec() {
        if (!preCheck()) {
            src.pushStage(this);
            exec();
        } else {
            doExec();
        }
        return this.dep;
    }

    public synchronized boolean exec() {
        if (dep == null || !preCheck()) {
            return false;
        }
        if (this.executor != null) {
            this.executor.execute(this);
            this.executor = null;
        }
        doExec();
        this.dep = null;
        return true;
    }

    public void run() {
        exec();
    }
}