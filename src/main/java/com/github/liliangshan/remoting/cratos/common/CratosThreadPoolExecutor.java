package com.github.liliangshan.remoting.cratos.common;

import java.util.concurrent.*;

/**
 * CratosThreadExecutor .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class CratosThreadPoolExecutor extends ThreadPoolExecutor {

    private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();
    private static final ThreadFactory defaultThreadFactory = new NamedThreadFactory();
    private static final BlockingQueue<Runnable> defaultWorkQueue = new LinkedBlockingQueue<>();
    public static final int DEFAULT_MAX_IDLE_TIME = 60 * 1000; // 1 minutes

    public CratosThreadPoolExecutor(int poolSize) {
        this(poolSize, poolSize);
    }

    public CratosThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        this(corePoolSize, maximumPoolSize, DEFAULT_MAX_IDLE_TIME, TimeUnit.MILLISECONDS, defaultWorkQueue);
    }

    public CratosThreadPoolExecutor(int corePoolSize, int maximumPoolSize, String threadPrefix) {
        this(corePoolSize, maximumPoolSize, DEFAULT_MAX_IDLE_TIME, TimeUnit.MILLISECONDS,
                defaultWorkQueue, new NamedThreadFactory(threadPrefix));
    }

    public CratosThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, defaultWorkQueue);
    }

    public CratosThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, defaultThreadFactory);
    }

    public CratosThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
    }

    public CratosThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, defaultThreadFactory, handler);
    }

    public CratosThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


}
