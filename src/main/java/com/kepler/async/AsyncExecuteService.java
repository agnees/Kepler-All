package com.kepler.async;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by 张皆浩 on 2016/12/30.
 * DIDI CORPORATION
 */
public class AsyncExecuteService extends ThreadPoolExecutor {

    private List<ThreadLocal> threadLocalList;

    public AsyncExecuteService(List<ThreadLocal> threadLocalList, int corePoolSize, int maximumPoolSize, long
            keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.threadLocalList = threadLocalList;
    }

    public AsyncExecuteService(List<ThreadLocal> threadLocalList, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.threadLocalList = threadLocalList;
    }

    public AsyncExecuteService(List<ThreadLocal> threadLocalList, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.threadLocalList = threadLocalList;
    }

    public AsyncExecuteService(List<ThreadLocal> threadLocalList, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.threadLocalList = threadLocalList;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return super.newTaskFor(new InherittableRunnable(runnable), value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return super.newTaskFor(new InherittableCallable<T>(callable));
    }

    abstract class ThreadLocalMixin {

        protected void before() {
            for (ThreadLocal threadLocal : threadLocalList) {
                Object tlType = threadLocal.get();
                if (tlType == null) {
                    continue;
                }
                for (Constructor<?> constructor : tlType.getClass().getConstructors()) {
                    Class<?>[] pTypes = constructor.getParameterTypes();
                    if (pTypes.length == 1 && pTypes[0].isAssignableFrom(tlType.getClass())) {
                        try {
                            Object threadValue = constructor.newInstance(tlType);
                            threadLocal.set(threadValue);
                        } catch (Exception e) {
                            // swallow
                        }
                        break;
                    }
                }
            }
        }

        protected void after() {
            for (ThreadLocal<?> threadLocal : threadLocalList) {
                threadLocal.remove();
            }
        }

    }

    class InherittableCallable<T> extends ThreadLocalMixin implements Callable<T> {

        private final Callable<T> c;

        public InherittableCallable(Callable<T> c) {
            this.c = c;
        }

        @Override
        public T call() throws Exception {
            try {
                before();
                return c.call();
            } finally {
                after();
            }
        }
    }

    class InherittableRunnable extends ThreadLocalMixin implements Runnable {

        private final Runnable r;

        public InherittableRunnable(Runnable r) {
            this.r = r;
        }

        @Override
        public void run() {
            try {
                before();
                r.run();
            } finally {
                after();
            }
        }

    }

}
