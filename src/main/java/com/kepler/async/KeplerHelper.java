package com.kepler.async;

/**
 * Created by 张皆浩 on 2017/1/5.
 * DIDI CORPORATION
 */
public class KeplerHelper {

    static ThreadLocal<Boolean> IS_OPEN = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    static ThreadLocal<ChainedFuture<?>> FUTURE = new ThreadLocal<ChainedFuture<?>>() {};

    static KeplerHelper asyncPromise() {
        KeplerHelper.IS_OPEN.set(true);
        KeplerHelper.FUTURE.set(new ChainedFutureImpl());
        return new KeplerHelper();
    }

    ChainedFuture<?> run(Object o) {
        KeplerHelper.IS_OPEN.set(false);
        return KeplerHelper.FUTURE.get();
    }

}
